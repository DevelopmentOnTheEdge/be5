package com.developmentontheedge.be5.meta;

import com.developmentontheedge.be5.metadata.model.Project;

import javax.inject.Provider;


public interface ProjectProvider extends Provider<Project>
{
    void reloadProject();

    void addToReload(Runnable runnable);
}
