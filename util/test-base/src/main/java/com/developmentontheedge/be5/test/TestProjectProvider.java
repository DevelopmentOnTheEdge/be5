package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.lifecycle.Start;
import com.developmentontheedge.be5.meta.ProjectProvider;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;


public class TestProjectProvider implements ProjectProvider
{
    private Project project;

    @Start(order = 10)
    public void start() throws Exception
    {
        try
        {
            project = ModuleLoader2.findAndLoadProjectWithModules(false);
            BaseTest.addH2Profile(project);
        }
        catch (ProjectLoadException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Project get()
    {
        return project;
    }

    @Override
    public void reloadProject()
    {

    }

    @Override
    public void addToReload(Runnable runnable)
    {

    }
}
