package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.Be5Caches;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.env.Stage;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.metadata.serialization.WatchDir;

import javax.inject.Provider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ProjectProviderImpl implements ProjectProvider
{
    private Project project;
    private Map<String, Project> initModulesMap;
    private List<Runnable> callOnReload = new ArrayList<>();

    private WatchDir watcher = null;

    private volatile boolean dirty = false;

    private final Stage stage;
    private final Be5Caches be5Caches;
    private final Provider<DatabaseService> databaseServiceProvider;

    public ProjectProviderImpl(Stage stage, Be5Caches be5Caches,
                               Provider<DatabaseService> databaseServiceProvider)
    {
        this.stage = stage;
        this.be5Caches = be5Caches;
        this.databaseServiceProvider = databaseServiceProvider;
    }

    public void addToReload(Runnable supplier)
    {
        callOnReload.add(supplier);
    }

    @Override
    synchronized public Project getProject()
    {
    	if(dirty || project == null)
    	{
    	    Project oldProject = project;
			project = loadProject();

            //String path = new File(".").getCanonicalPath();
			//CopyGroovy.copyFolder();

            if(oldProject != null)
            {
                be5Caches.clearAll();
                callOnReload.forEach(Runnable::run);
                updateDatabaseSystem();
            }
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

            //todo move to ModuleLoader2 - find dev.yaml only in current project,
            if (stage == Stage.DEVELOPMENT)
            {
                if(initModulesMap == null)initModulesMap = ModuleLoader2.getModulesMap();
                watcher = new WatchDir(initModulesMap)
                    .onModify( onModify -> dirty = true)
                    .start();
            }

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
    public synchronized void reloadProject()
    {
        this.dirty = true;
        getProject();
    }

    private void updateDatabaseSystem()
    {
        project.setDatabaseSystem(databaseServiceProvider.get().getRdbms());
    }

}
