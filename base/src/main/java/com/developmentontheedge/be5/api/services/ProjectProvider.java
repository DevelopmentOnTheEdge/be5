package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.metadata.model.Project;

public interface ProjectProvider
{
    /**
     * Returns a meta-model of the web application. Don't keep the project in your service directly,
     * as services are created once and live forever,
     * but the project can be reloaded.
     */
    Project getProject();

    void reloadProject();

    void addToReload(Runnable runnable);
}
