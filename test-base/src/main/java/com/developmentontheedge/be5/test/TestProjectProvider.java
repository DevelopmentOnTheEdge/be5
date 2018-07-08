package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;

import javax.inject.Inject;


public class TestProjectProvider implements ProjectProvider
{
    public static final String profileForIntegrationTests = "profileForIntegrationTests";

    private Project project;

    @Inject
    public TestProjectProvider()
    {
        try
        {
            project = ModuleLoader2.findAndLoadProjectWithModules(false);
            BaseTestUtils.addH2Profile(project);
        } catch (ProjectLoadException e)
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
