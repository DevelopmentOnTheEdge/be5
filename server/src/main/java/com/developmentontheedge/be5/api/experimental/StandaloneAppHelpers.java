package com.developmentontheedge.be5.api.experimental;

import com.developmentontheedge.dbms.DbmsConnector;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.services.impl.ConstantDatabaseService;
import com.developmentontheedge.be5.api.services.impl.SqlServiceImpl;

/**
 * It's experimental and temporary class to manually create services from your legacy code.
 * 
 * @author asko
 */
public class StandaloneAppHelpers
{
    
    public static SqlService createSqlService(DbmsConnector connector)
    {
        return new SqlServiceImpl(new ConstantDatabaseService(connector));
    }
    
}
