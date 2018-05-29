package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.database.DataSourceService;
import com.developmentontheedge.sql.format.Dbms;

import javax.sql.DataSource;


public class DataSourceServiceMock implements DataSourceService
{
    @Override
    public DataSource getDataSource()
    {
        return null;
    }

    @Override
    public Dbms getDbms() {
        return Dbms.H2;
    }

}
