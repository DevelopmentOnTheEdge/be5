package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.dbms.DbmsConnector;

import javax.sql.DataSource;

public interface DatabaseService
{
    
    /**
     * Returns a connector for the project. 
     */
    DbmsConnector getDbmsConnector();

    DataSource getDataSource();

    int getNumIdle();

    int getNumActive();

    String getConnectionsStatistics();
}
