package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.Be5Caches;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.GroovyRegister;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.metadata.serialization.WatchDir;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import com.developmentontheedge.be5.util.CopyGroovy;

import java.io.File;
import java.io.IOException;
import java.util.Map;


public class ProjectProviderImpl implements ProjectProvider
{
    private Project project;
    private Be5Caches be5Caches;
    private Injector injector;
    private Map<String, Project> initModulesMap;

    private WatchDir watcher = null;

    private volatile boolean dirty = false;

    //private DatabaseService databaseService;

    public ProjectProviderImpl(Injector injector, Be5Caches be5Caches)
    {
        this.injector = injector;//todo remove injector, use @Inject, fix resolve
        this.be5Caches = be5Caches;
        //this.databaseService = databaseService;
    }

    @Override
    synchronized public Project getProject()
    {
    	if(dirty || project == null)
    	{
			project = loadProject();

            //String path = new File(".").getCanonicalPath();
			//CopyGroovy.copyFolder();

            be5Caches.clearAll();
            injector.get(UserAwareMeta.class).reCompileLocalizations();
            GroovyRegister.initClassLoader();
            updateDatabaseSystem();
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
            if (ProjectProviderImpl.class.getClassLoader().getResource("dev.yaml") != null)
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
    public void reloadProject()
    {
        this.dirty = true;
        getProject();
    }

    @Override
    public void updateDatabaseSystem()
    {
        project.setDatabaseSystem(injector.get(DatabaseService.class).getRdbms());
    }

}
