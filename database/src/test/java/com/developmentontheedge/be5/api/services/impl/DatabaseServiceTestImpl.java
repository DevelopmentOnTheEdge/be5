package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.sql.format.Dbms;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class DatabaseServiceTestImpl implements DatabaseService
{
    private final DataSource dataSource;

    public DatabaseServiceTestImpl(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    @Override
    public Dbms getDbms()
    {
        return Dbms.H2;
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        return dataSource.getConnection();
    }

    @Override
    public Map<String, String> getParameters()
    {
        return new HashMap<>();
    }

}
