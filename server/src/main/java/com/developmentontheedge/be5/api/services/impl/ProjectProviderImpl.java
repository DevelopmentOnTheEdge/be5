package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.CacheInfo;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.metadata.serialization.WatchDir;

import java.io.IOException;

public class ProjectProviderImpl implements ProjectProvider
{
    private Project project;
    private Injector injector;

    private WatchDir watcher = null;

    private volatile boolean dirty = false;

    public ProjectProviderImpl(LogConfigurator logConfigurator, Injector injector)
    {
        this.injector = injector;
    }

    @Override
    synchronized public Project getProject()
    {
    	if(dirty || project == null)
    	{
			project = loadProject();
            CacheInfo.clearAll();
            injector.get(UserAwareMeta.class).reCompileLocalizations(injector);
        }

    	return project;
    }

    private Project loadProject() 
    {
        try
        {
            if(watcher != null)watcher.stop();

            Project project = ModuleLoader2.findAndLoadProjectWithModules();

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

    @Override
    public void reloadProject()
    {
        this.dirty = true;
        getProject();
    }

}
