package com.developmentontheedge.be5.database.test;

import com.developmentontheedge.be5.meta.ProjectProvider;
import com.developmentontheedge.be5.metadata.model.Project;


public class EmptyTestProjectProvider implements ProjectProvider
{
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
