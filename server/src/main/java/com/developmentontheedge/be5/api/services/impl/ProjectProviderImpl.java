package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.Be5Caches;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.metadata.serialization.WatchDir;

import java.io.IOException;
import java.util.Map;

public class ProjectProviderImpl implements ProjectProvider
{
    private Project project;
    private Injector injector;
    private Map<String, Project> initModulesMap;

    private WatchDir watcher = null;

    private volatile boolean dirty = false;

    public ProjectProviderImpl(Injector injector)
    {
        this.injector = injector;
    }

    @Override
    synchronized public Project getProject()
    {
    	if(dirty || project == null)
    	{
			project = loadProject();
            Be5Caches.clearAll();
            injector.get(UserAwareMeta.class).reCompileLocalizations();
        }

    	return project;
    }

    private Project loadProject() 
    {
        try
        {
            if(watcher != null)watcher.stop();
            Project newProject = null;

            try{
                newProject = ModuleLoader2.findAndLoadProjectWithModules();
            }catch (RuntimeException e){
                System.out.println("Can't load project.\n" + e.toString());
                if(project == null){
                    e.printStackTrace();
                    System.exit(0);
                }
            }

            if(initModulesMap == null)initModulesMap = ModuleLoader2.getModulesMap();

            watcher = new WatchDir(initModulesMap)
                    .onModify( onModify -> dirty = true)
                    .start();

            return newProject != null ? newProject : project;
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

    @Override
    public void reloadProject()
    {
        this.dirty = true;
        getProject();
    }

}
