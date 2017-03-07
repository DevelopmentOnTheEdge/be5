package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.dbms.DbmsConnector;
import com.developmentontheedge.be5.api.services.DatabaseService;

public class ConstantDatabaseService implements DatabaseService
{
    
    private final DbmsConnector connector;

    public ConstantDatabaseService(DbmsConnector connector)
    {
        this.connector = connector;
    }

    @Override
    public DbmsConnector getDatabaseConnector()
    {
        return connector;
    }
    
}