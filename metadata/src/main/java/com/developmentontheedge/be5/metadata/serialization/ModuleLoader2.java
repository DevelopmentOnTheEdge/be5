package com.developmentontheedge.be5.metadata.serialization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.exception.ReadException;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.ProjectFileStructure;
import com.developmentontheedge.be5.metadata.util.ProcessController;

public class ModuleLoader2
{
    private static final Logger log = Logger.getLogger(ModuleLoader2.class.getName());

    private static String PROJECT_FILE_NAME = ProjectFileStructure.PROJECT_FILE_NAME_WITHOUT_SUFFIX + ProjectFileStructure.FORMAT_SUFFIX;
    
    private static Map<String, Path> modulesMap;
    
    private static synchronized void init()
    {
        if( modulesMap != null )
            return;

        modulesMap= new HashMap<>();

        try
        {
            Enumeration<URL> urls = (ModuleLoader2.class).getClassLoader().getResources(PROJECT_FILE_NAME);
            URL url;
            while( urls.hasMoreElements() )
            {
                url = urls.nextElement();
                final String name = parse(url);
                
                String ext = url.toExternalForm();
                String jar = ext.substring(0, ext.indexOf('!'));
                FileSystem fs = FileSystems.newFileSystem(URI.create(jar), new HashMap<String, String>());
                Path p = fs.getPath("./");
                modulesMap.put(name, p);
                log.info("Module: " + name + "\text=" + url.toExternalForm() + ", path=" + p);
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public static String parse(URL url) throws IOException
    {
        try(
                InputStream in = url.openStream();
                BufferedReader r = new BufferedReader(new InputStreamReader(in, "utf-8"));
            )
        {
            String ln = r.readLine();
            return ln.substring(0, ln.indexOf(':')).trim();
        }
        catch (IOException x) 
        {
            //System.out.println( "Error reading configuration file", x);
        }
        
        return null;
    }
    
    public static boolean containsModule(String name)
    {
        init();
        
        return modulesMap.containsKey(name);
    }
    
    public static Path resolveModule(String name)
    {
        init();
        
        return modulesMap.get(name);
    }

    public static Project loadModule(String name, LoadContext context) throws ProjectLoadException
    {
        init();

    	if( ! containsModule(name))
            throw new IllegalArgumentException("Module not found: " + name);

        return Serialization.load(modulesMap.get(name), true, context );
    }

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
                Project moduleProject = loadModule( module.getName(), loadContext );
                result.add( moduleProject );
            }
        }
        Collections.sort( result, ( o1, o2 ) -> {
            if ( o1.getModules().contains( o2.getName() ) )
                return 1;
            if ( o2.getModules().contains( o1.getName() ) )
                return -1;
            return 0;
        } );
        return result;
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
        Path modulePath = ModuleLoader2.resolveModule(moduleName);
        if ( modulePath != null )
        {
            Project project = new Project( moduleName );
            project.setLocation( modulePath );
            project.setProjectFileStructure( new ProjectFileStructure( project ) );
            return new ProjectFileSystem( project );
        }

        return null;
    }
}
