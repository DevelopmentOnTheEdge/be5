package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.dbms.DbmsConnector;
import com.developmentontheedge.be5.api.impl.Be5;
import com.developmentontheedge.be5.api.services.DatabaseService;

public class DatabaseServiceImpl implements DatabaseService
{
    
    @Override
    public DbmsConnector getDatabaseConnector()
    {
        return Be5.getDatabaseConnector();
    }

}
