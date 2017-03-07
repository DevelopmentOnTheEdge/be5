package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.metadata.sql.DatabaseConnector;

public interface DatabaseService
{
    
    /**
     * Returns a connector for the project. 
     */
    DatabaseConnector getDatabaseConnector();
    
}
