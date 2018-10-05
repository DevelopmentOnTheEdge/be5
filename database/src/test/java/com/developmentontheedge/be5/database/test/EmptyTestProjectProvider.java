package com.developmentontheedge.be5.database.test;

import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;

import javax.inject.Inject;


public class EmptyTestProjectProvider implements ProjectProvider
{
    @Inject
    public EmptyTestProjectProvider()
    {
    }


    @Override
    public Project get()
    {
        return null;
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
