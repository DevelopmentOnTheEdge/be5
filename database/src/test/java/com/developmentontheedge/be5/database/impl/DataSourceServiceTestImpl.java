package com.developmentontheedge.be5.database.impl;

import com.developmentontheedge.be5.database.DataSourceService;
import com.developmentontheedge.sql.format.Dbms;

import javax.sql.DataSource;


public class DataSourceServiceTestImpl implements DataSourceService
{
    private final DataSource dataSource;

    public DataSourceServiceTestImpl(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    @Override
    public Dbms getDbms()
    {
        return Dbms.H2;
    }

    @Override
    public DataSource getDataSource()
    {
        return dataSource;
    }

}