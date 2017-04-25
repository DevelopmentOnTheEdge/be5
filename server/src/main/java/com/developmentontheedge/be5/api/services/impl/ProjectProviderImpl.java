package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.ProjectFileStructure;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.serialization.WatchDir;
import one.util.streamex.StreamEx;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ProjectProviderImpl implements ProjectProvider
{
    private Logger log = Logger.getLogger(ProjectProviderImpl.class.getName());
    
    private static String PROJECT_FILE_NAME = ProjectFileStructure.PROJECT_FILE_NAME_WITHOUT_SUFFIX + ProjectFileStructure.FORMAT_SUFFIX;
    private Project project;

    private WatchDir watcher = null;
    private volatile boolean dirty = false;
    
    @Override
    synchronized public Project getProject()
    {
    	if(dirty || project == null)
    	{
			project = loadProject();
    	}

    	return project;
    }

    private Project loadProject() 
    {
        long startTime = System.nanoTime();

        try
        {
            if(watcher != null)
                watcher.stop();

            LoadContext loadContext = new LoadContext();

            List<Project> availableModulesAndProjects = loadModulesAndProject(loadContext);

            Project project = getProject(availableModulesAndProjects);
            List<Project> modulesForProject = getModulesForProject(project, availableModulesAndProjects);

            ModuleLoader2.mergeAllModules(project, modulesForProject, loadContext);
            loadContext.check();
            
            // TODO - check
            watcher = new WatchDir(project).onModify( onModify -> dirty = true).start();

            logLoadedProject(project, startTime);
            return project;
        }
        catch(Throwable t)
        {
        	log.severe("Can not load project, error: " + t.getMessage());
        	throw Be5Exception.internal(t, "Can not load project");
        }
        finally
        {
            dirty = false;
        }
    }

    List<Project> loadModulesAndProject(LoadContext loadContext) throws ProjectLoadException, IOException, URISyntaxException
    {
        ArrayList<URL> urls = Collections.list(getClass().getClassLoader().getResources(PROJECT_FILE_NAME));
        if( urls.isEmpty() )
        {
            throw Be5Exception.internal("Modules is not found in classpath or war file.");
        }

        List<Project> modules = new ArrayList<>();
        for (URL url: urls)
        {
            Project module;
            String ext = url.toExternalForm();
            if( ext.indexOf('!') < 0 ) // usual file in directory
            {
                Path path = Paths.get(url.toURI()).getParent();
                module = Serialization.load(path, loadContext);
                log.info("Load module from dir: " + path);
            }
            else // war or jar file
            {
                String jar = ext.substring(0, ext.indexOf('!'));
                FileSystem fs = FileSystems.newFileSystem(URI.create(jar), new HashMap<String, String>());
                Path path = fs.getPath("./");
                module = Serialization.load(path, loadContext);
                fs.close();

                log.info("Load module from " + url.toExternalForm() + ", path=" + path);
            }
            if(module!= null)modules.add(module);
        }
        return modules;
    }

    Project getProject(List<Project> availableModulesAndProjects)
    {
        Project project = null;
        for (Project module: availableModulesAndProjects)
        {
            if(module != null && !module.isModuleProject())
            {
                if(project != null)
                {
                    throw Be5Exception.internal("Several projects were found: " + project + ", " + module);
                }
                else
                {
                    project = module;
                }
            }
        }
        if(project == null){
            throw Be5Exception.internal("Project is not found in load modules.");
        }
        return project;
    }

    List<Project> getModulesForProject(Project project, List<Project> availableModulesAndProjects)
    {
        List<Project> modules = StreamEx.of(availableModulesAndProjects)
                .filter(module -> module != null && module.isModuleProject())
                .filter(module -> project.getModules().contains(module.getName())).toList();

        for (Module requiredModule : project.getModules())
        {
            if (!modules.contains(requiredModule))
            {
                throw Be5Exception.internal("Required module " + requiredModule + " not load.");
            }
        }

        return modules;
    }

    private void logLoadedProject(Project project, long startTime)
    {
        StringBuilder sb = new StringBuilder("Project loaded, name='" + project.getName() +
                "', loading time: " + TimeUnit.NANOSECONDS.toMillis( System.nanoTime()-startTime ) + " ms");

        if(project.getModules().getSize()>0)
        {
            sb.append("\nModules: ");
            for (Module module : project.getModules())
            {
                sb.append("\n - "); sb.append(module.getName());
            }
        }
        log.info(sb.toString());
    }
    
}
