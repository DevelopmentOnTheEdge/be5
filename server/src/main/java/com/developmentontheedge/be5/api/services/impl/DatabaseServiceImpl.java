package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.impl.Be5;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.dbms.DbmsConnector;

public class DatabaseServiceImpl implements DatabaseService
{
    
    @Override
    public DbmsConnector getDbmsConnector()
    {
        return Be5.getDbmsConnector();
    }

}
