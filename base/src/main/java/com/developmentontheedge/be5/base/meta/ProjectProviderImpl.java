package com.developmentontheedge.be5.base.meta;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.lifecycle.Start;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.metadata.serialization.WatchDir;
import com.google.inject.Stage;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ProjectProviderImpl implements ProjectProvider
{
    private Project project;
    private List<Runnable> callOnReload = new ArrayList<>();

    private WatchDir watcher = null;

    private volatile boolean dirty = false;

    private final Stage stage;

    @Inject
    public ProjectProviderImpl(Stage stage)
    {
        this.stage = stage;
    }

    @Start(order = 10)
    public void start()
    {
        project = loadProject();
    }

    @Override
    public void addToReload(Runnable supplier)
    {
        callOnReload.add(supplier);
    }

    @Override
    public synchronized Project get()
    {
        if (dirty)
        {
            Project oldProject = project;
            project = loadProject();

            if (oldProject != null)
            {
                callOnReload.forEach(Runnable::run);
            }
        }

        return project;
    }

    private Project loadProject()
    {
        try
        {
            if (watcher != null) watcher.stop();

            try
            {
                Project project = ModuleLoader2.findAndLoadProjectWithModules(dirty);
                dirty = false;
                return project;
            }
            catch (RuntimeException e)
            {
                throw Be5Exception.internal("Can't load project", e);
            }
            finally
            {
                if (stage == Stage.DEVELOPMENT)
                {
                    if (ModuleLoader2.getModulesMap() != null)
                    {
                        watcher = new WatchDir(ModuleLoader2.getModulesMap())
                                .onModify(onModify -> dirty = true)
                                .start();
                    }
                }
            }
        }
        catch (ProjectLoadException | IOException e)
        {
            throw Be5Exception.internal("Can not load project", e);
        }
    }

    @Override
    public synchronized void reloadProject()
    {
        this.dirty = true;
        get();
    }

}
