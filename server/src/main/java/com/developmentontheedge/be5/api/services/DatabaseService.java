package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.dbms.DbmsConnector;

public interface DatabaseService
{
    
    /**
     * Returns a connector for the project. 
     */
    DbmsConnector getDatabaseConnector();
    
}
