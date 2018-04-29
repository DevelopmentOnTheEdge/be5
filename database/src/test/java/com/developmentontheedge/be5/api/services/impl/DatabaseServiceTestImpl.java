package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.sql.format.Dbms;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;


public class DatabaseServiceTestImpl implements DatabaseService
{
    private final DataSource dataSource;
    //private Rdbms type;

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
        Map<String, String> map = new TreeMap<>();

        if(dataSource instanceof BasicDataSource)
        {
            BasicDataSource dataSource = (BasicDataSource)this.dataSource;
            map.put("DataSource class", dataSource.getClass().getCanonicalName());
            map.put("Active/Idle", dataSource.getNumActive() + " / " + dataSource.getNumIdle());
            map.put("max Active/max Idle", dataSource.getMaxActive() + " / " + dataSource.getMaxIdle());
            map.put("max wait", dataSource.getMaxWait() + "");
            map.put("Username", dataSource.getUsername());
            map.put("DefaultCatalog", dataSource.getDefaultCatalog());
            map.put("DriverClassName", dataSource.getDriverClassName());
            map.put("Url", dataSource.getUrl());
            //map.put("JmxName", dataSource.getJmxName());
            map.put("ValidationQuery", dataSource.getValidationQuery());
            //map.put("EvictionPolicyClassName", dataSource.getEvictionPolicyClassName());
            map.put("ConnectionInitSqls", dataSource.getConnectionInitSqls().toString());

        }

        return map;
    }

}
