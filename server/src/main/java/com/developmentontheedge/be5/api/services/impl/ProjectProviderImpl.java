package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.metadata.serialization.WatchDir;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ProjectProviderImpl implements ProjectProvider
{
    private static final Logger log = Logger.getLogger(ProjectProviderImpl.class.getName());
    
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
        try
        {
            if(watcher != null)watcher.stop();

            long startTime = System.nanoTime();
            Project project = ModuleLoader2.findAndLoadProjectWithModules();
            log.info(ModuleLoader2.logLoadedProject(project, startTime));

            watcher = new WatchDir(project).onModify( onModify -> dirty = true).start();

            return project;
        }
        catch(ProjectLoadException | IOException e)
        {
        	throw Be5Exception.internal(e, "Can not load project");
        }
        finally
        {
            dirty = false;
        }
    }

    /**
     * For hot reload
     * @param urls projects URL
     */
    private void replaceURLtoSource(ArrayList<URL> urls)
    {
        try
        {
            Map<String, String> modulesSource = readDevPathsToSourceProjects();
            StringBuilder sb = new StringBuilder("Replace project path for hot reload:");
            boolean started = false;
            for (int i = 0; i < urls.size(); i++)
            {
                for (Map.Entry<String, String> moduleSource : modulesSource.entrySet())
                {
                    String name = getProjectName(urls.get(i));
                    if (name.equals(moduleSource.getKey()))
                    {
                        started = true;
                        urls.set(i, Paths.get(moduleSource.getValue()).toUri().toURL());
                        sb.append("\n - ").append(name).append(": ").append(urls.get(i));
                    }
                }
            }
            if(started)log.info(sb.toString());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private String getProjectName(URL url) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
        Map<String, Object> module = (Map<String, Object>)new Yaml().load(reader);
        return module.entrySet().iterator().next().getKey();
    }

    @SuppressWarnings("unchecked")
    Map<String, String> readDevPathsToSourceProjects() throws IOException
    {
        ArrayList<URL> urls = Collections.list(getClass().getClassLoader().getResources("dev.yaml"));
        if(urls.size() == 1){
            BufferedReader reader = new BufferedReader(new InputStreamReader(urls.get(0).openStream(), "utf-8"));
            List<Map<String, String>> modulesTemp = ( List<Map<String, String>> ) ((Map<String, Object>) new Yaml().load(reader)).get("pathsToSourceProjects");
            if(modulesTemp == null)return new HashMap<>();
            Map<String, String> modules = new HashMap<>();
            for (Map<String, String> element: modulesTemp)
            {
                Map.Entry<String, String> entry = element.entrySet().iterator().next();
                modules.put(entry.getKey(), entry.getValue());
            }
            return modules;
        }
        return new HashMap<>();
    }

}
