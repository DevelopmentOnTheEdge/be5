package com.developmentontheedge.be5.metadata.serialization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
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
import org.yaml.snakeyaml.Yaml;

public class ModuleLoader2
{
    private static final Logger log = Logger.getLogger(ModuleLoader2.class.getName());

    private static Map<String, Project> modulesMap;
    private static Map<String, Path> pathsToProjectsToHotReload = new HashMap<>();

    public static Map<String, Project> getModulesMap()
    {
        return modulesMap;
    }

    private static synchronized void loadAllProjects(boolean dirty)
    {
        if( modulesMap != null && !dirty)
            return;

        modulesMap = new HashMap<>();

        try
        {
            ArrayList<URL> urls = Collections.list(ModuleLoader2.class.getClassLoader().getResources(
                    ProjectFileStructure.PROJECT_FILE_NAME_WITHOUT_SUFFIX + ProjectFileStructure.FORMAT_SUFFIX));

            replaceAndAddURLtoSource(urls);

            for (URL url : urls)
            {
                LoadContext loadContext = new LoadContext();

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
                    FileSystem fs;// = FileSystems.getFileSystem(URI.create(jar));

                    try {
                        fs = FileSystems.newFileSystem(URI.create(jar), Collections.emptyMap());
                    } catch (FileSystemAlreadyExistsException e) {
                        fs = FileSystems.getFileSystem(URI.create(jar));
                        log.info("Get exists FileSystem after exception");
                    }

                    Path path = fs.getPath("./");
                    module = Serialization.load(path, loadContext);

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
        loadAllProjects(false);
        
        return modulesMap.containsKey(name);
    }
    
    public static Path getModulePath(String name)
    {
        loadAllProjects(false);
        
        return modulesMap.get(name).getLocation();
    }

    public static Project findAndLoadProjectWithModules() throws ProjectLoadException
    {
        loadAllProjects(true);

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

        if(project == null)
        {
            //todo create new not module project for tests?
            project = new ProjectTopologicalSort(modulesMap.values()).getRoot();
        }

        ModuleLoader2.mergeModules(project, new JULLogger(log));

        return project;
    }

    public static void addModuleScripts( Project project ) throws ReadException
    {
        loadAllProjects(false);

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
        long startTime = System.nanoTime();
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
        log.info(ModuleLoader2.logLoadedProject(be5Project, startTime));
    }

    private static void mergeAllModules(
        final Project model,
        final ProcessController logger,
        final LoadContext context ) throws ProjectLoadException
    {
        mergeAllModules(model, loadModules(model, logger, context), context);
    }

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

    private static Project foldModules( final List<Project> modules, LoadContext context )
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
        StringBuilder sb = new StringBuilder();
        if(project.isModuleProject())
        {
            sb.append(JULLogger.infoBlock("Loaded module:"));
        }
        else
        {
            sb.append(JULLogger.infoBlock("Loaded project:"));
        }

        sb.append("\nName: ").append(project.getName());

        if(project.getModules().getSize()>0)
        {
            sb.append("\nModules: ");
            for (Module module : project.getModules())
            {
                sb.append("\n - "); sb.append(module.getName());
            }
        }
        sb.append("\nLoading time: ")
                .append(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)).append(" ms");
        sb.append("\n");
        return sb.toString();
    }

    /**
     * For hot reload
     * @param urls projects URL
     */
    private static void replaceAndAddURLtoSource(ArrayList<URL> urls)
    {
        try
        {
            Map<String, Path> modulesSource = readDevPathsToSourceProjects();
            if(modulesSource.isEmpty() && pathsToProjectsToHotReload.isEmpty())return;

            StringBuilder sb = new StringBuilder();
            sb.append(JULLogger.infoBlock("Replace project path for hot reload (dev.yaml):"));
            boolean started = false;

            for (Map.Entry<String, Path> moduleSource : modulesSource.entrySet())
            {
                boolean used = false;
                for (int i = 0; i < urls.size(); i++)
                {
                    String name = getProjectName(urls.get(i));
                    if (name.equals(moduleSource.getKey()))
                    {
                        used = started = true;
                        urls.set(i, moduleSource.getValue().toUri().toURL());
                        sb.append("\n - ").append(String.format("%-15s", name)).append(urls.get(i)).append(" - replace");
                    }
                }
                if(!used)
                {
                    URL url = moduleSource.getValue().toUri().toURL();
                    urls.add(url);
                    sb.append("\n - ").append(moduleSource.getKey()).append(": ").append(url).append(" - add");
                }
            }
            sb.append("\n");
            if(started)log.info(sb.toString());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private static String getProjectName(URL url) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
        Map<String, Object> module = (Map<String, Object>)new Yaml().load(reader);
        return module.entrySet().iterator().next().getKey();
    }

    /**
     * dev.yaml example:
     * pathsToSourceProjects:
     * - testBe5app: /home/uuinnk/workspace/github/testapp/project.yaml
     *
     * @return Map name -> source path of modules
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Path> readDevPathsToSourceProjects() throws IOException
    {
        ArrayList<URL> urls = Collections.list(ModuleLoader2.class.getClassLoader().getResources("dev.yaml"));
        if(urls.size() > 1)
        {
            throw new RuntimeException("dev.yaml should be only in the project.");
        }

        if(urls.size() == 1)
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(urls.get(0).openStream(), "utf-8"));
            Map<String, Object> content = (Map<String, Object>) new Yaml().load(reader);

            initPathsForDev(content);

            Map<String, Path> modules = new HashMap<>();

            {//deprecated
                List<Map<String, String>> modulesTemp = ( List<Map<String, String>> ) content.get("pathsToSourceProjects");
                if(modulesTemp == null)return new HashMap<>();

                for (Map<String, String> element: modulesTemp)
                {
                    Map.Entry<String, String> entry = element.entrySet().iterator().next();
                    modules.put(entry.getKey(), Paths.get(entry.getValue()));
                }
            }

            List<Map<String, String>> modulesTemp = ( List<Map<String, String>> ) content.get("paths");
            if(modulesTemp == null)return new HashMap<>();
            for (Map<String, String> element: modulesTemp)
            {
                Map.Entry<String, String> entry = element.entrySet().iterator().next();
                modules.put(entry.getKey(), Paths.get(entry.getValue() + "/project.yaml"));
            }

            return modules;
        }
        return new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    private static void initPathsForDev(Map<String, Object> content)
    {
        List<Map<String, String>> paths = ( List<Map<String, String>> ) content.get("paths");
        if(paths != null)
        {
            for (Map<String, String> element : paths)
            {
                Map.Entry<String, String> entry = element.entrySet().iterator().next();
                pathsToProjectsToHotReload.put(entry.getKey(), Paths.get(entry.getValue()));
            }
        }
    }

    public static Map<String, Path> getPathsToProjectsToHotReload()
    {
        return pathsToProjectsToHotReload;
    }
}
