package com.developmentontheedge.be5.metadata.serialization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.exception.ReadException;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.ProjectFileStructure;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import com.developmentontheedge.be5.metadata.util.ProcessController;

public class ModuleLoader2
{
    private static final Logger log = Logger.getLogger(ModuleLoader2.class.getName());

    private static Map<String, Project> modulesMap;
    
    private static synchronized void init()
    {
        if( modulesMap != null )
            return;

        modulesMap = new HashMap<>();

        try
        {
            Enumeration<URL> urls = (ModuleLoader2.class).getClassLoader().getResources(
                    ProjectFileStructure.PROJECT_FILE_NAME_WITHOUT_SUFFIX + ProjectFileStructure.FORMAT_SUFFIX);

            URL url;
            while( urls.hasMoreElements() )
            {
                LoadContext loadContext = new LoadContext();
                url = urls.nextElement();

                Project module;
                String ext = url.toExternalForm();
                if( ext.indexOf('!') < 0 ) // usual file in directory
                {
                    Path path = Paths.get(url.toURI()).getParent();
                    module = Serialization.load(path, loadContext);
                    log.fine("Load module from dir: " + path);
                }
                else // war or jar file
                {
                    String jar = ext.substring(0, ext.indexOf('!'));
                    FileSystem fs = FileSystems.newFileSystem(URI.create(jar), new HashMap<String, String>());
                    Path path = fs.getPath("./");
                    module = Serialization.load(path, loadContext);
                    //TODO close if not maven script fs.close();

                    log.fine("Load module from " + url.toExternalForm() + ", path=" + path);
                }
                loadContext.check();
                modulesMap.put(module.getAppName(), module);
            }
        }
        catch (ProjectLoadException | IOException | URISyntaxException e){
            e.printStackTrace();
        }
    }
    
    public static String parse(URL url) throws IOException
    {
        try(InputStream in = url.openStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(in, "utf-8")))
        {
            String ln = r.readLine();
            return ln.substring(0, ln.indexOf(':')).trim();
        }
    }
    
    public static boolean containsModule(String name)
    {
        init();
        
        return modulesMap.containsKey(name);
    }
    
    public static Path getModulePath(String name)
    {
        init();
        
        return modulesMap.get(name).getLocation();
    }

    public static Project findAndLoadProjectWithModules() throws ProjectLoadException {
        init();

        Project project = null;
        for (Map.Entry<String,Project> module: modulesMap.entrySet())
        {
            if(module.getValue() != null && !module.getValue().isModuleProject())
            {
                if(project != null)
                {
                    throw new RuntimeException("Several projects were found: " + project + ", " + module);
                }
                else
                {
                    project = module.getValue();
                }
            }
        }
        if(project == null){
            throw new RuntimeException("Project is not found in load modules.");
        }
        ModuleLoader2.mergeModules(project, new JULLogger(log));

        return project;
    }

//    public static Project loadModule(String name, LoadContext context) throws ProjectLoadException
//    {
//        init();
//
//    	if( ! containsModule(name))
//            throw new IllegalArgumentException("Module not found: " + name);
//
//        return Serialization.load(modulesMap.get(name), true, context );
//    }

    public static void addModuleScripts( Project project ) throws ReadException
    {
        init();

        for ( Module module : project.getModules() )
        {
            Serialization.loadModuleMacros(module);
        }
    }

    public static List<Project> loadModules( Project application, ProcessController logger, LoadContext loadContext ) throws ProjectLoadException
    {
        List<Project> result = new ArrayList<>();
        for ( Module module : application.getModules() )
        {
            if ( containsModule(module.getName()) )
            {
                if ( logger != null )
                {
                    logger.setOperationName( "Reading module " + module.getName() + "..." );
                }
                Project moduleProject = modulesMap.get(module.getName());
                result.add( moduleProject );
            }
        }
        result.sort((o1, o2) -> {
            if (o1.getModules().contains(o2.getName()))
                return 1;
            if (o2.getModules().contains(o1.getName()))
                return -1;
            return 0;
        });
        return result;
    }

    public static void mergeModules(Project be5Project, ProcessController logger) throws ProjectLoadException
    {
        LoadContext loadContext = new LoadContext();
        try
        {
            ModuleLoader2.mergeAllModules( be5Project, logger, loadContext );
        }
        catch(ProjectLoadException e)
        {
            throw new ProjectLoadException("Merge modules", e);
        }
        loadContext.check();
    }

    /**
     * 
     * @param model
     * @throws ProjectLoadException
     */
    public static void mergeAllModules(
        final Project model,
        final ProcessController logger,
        final LoadContext context ) throws ProjectLoadException
    {
        mergeAllModules(model, loadModules(model, logger, context), context);
    }

    /**
     * 
     * @param model
     * @throws ProjectLoadException
     */
    public static void mergeAllModules( final Project model, List<Project> modules, final LoadContext context ) throws ProjectLoadException
    {
        modules = new LinkedList<>( modules );

        for ( Project module : modules )
        {
            module.mergeHostProject( model );
        }

        final Project compositeModule = foldModules( modules, context );
        if ( compositeModule != null )
        {
            model.merge( compositeModule );
        }
    }

    public static Project foldModules( final List<Project> modules, LoadContext context )
    {
        if ( modules.isEmpty() )
        {
            return null;
        }

        Project compositeModule = null;

        for ( Project module : modules )
        {
            if ( compositeModule == null )
            {
                module.applyMassChanges( context );
                compositeModule = module;
            }
            else
            {
                module.getModules().merge( compositeModule.getModules(), true, false );
                module.getApplication().merge( compositeModule.getModule( module.getProjectOrigin() ), true, false );
                module.applyMassChanges( context );
                compositeModule = module;
            }
            if ( compositeModule.isModuleProject() )
            {
                DataElementUtils.addQuiet( module.getModules(), module.getApplication() );
                module.setApplication( null );
            }
        }

        return compositeModule;
    }

    /**
     * Returns BeanExplorerProjectFileSystem for given module if possible
     */
    public static ProjectFileSystem getFileSystem( Project app, String moduleName )
    {
        if ( app.getProjectOrigin().equals( moduleName ) )
        {
            return new ProjectFileSystem( app );
        }
        Path modulePath = ModuleLoader2.getModulePath(moduleName);
        if ( modulePath != null )
        {
            Project project = new Project( moduleName );
            project.setLocation( modulePath );
            project.setProjectFileStructure( new ProjectFileStructure( project ) );
            return new ProjectFileSystem( project );
        }

        return null;
    }

    public static String logLoadedProject(Project project, long startTime)
    {
        StringBuilder sb = new StringBuilder("Project loaded:\n" + project.getName());

        if(project.getModules().getSize()>0)
        {
            sb.append("\nModules: ");
            for (Module module : project.getModules())
            {
                sb.append("\n - "); sb.append(module.getName());
            }
        }
        sb.append("\nloading time: ")
                .append(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)).append(" ms");
        //log.info(sb.toString());
        return sb.toString();
    }
}
