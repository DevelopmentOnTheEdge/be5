package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.metadata.model.Project;

public interface ProjectProvider
{
    public static final String dataSourceName = "dataSourceNameBe5";

    /**
     * Returns a metamodel of the web application. Don't keep the project in your service directly,
     * as services are created once and live forever,
     * but the project can be reloaded.
     */
    Project getProject();
    
}
