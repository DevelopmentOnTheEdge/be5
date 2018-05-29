package com.developmentontheedge.be5.database;

import com.developmentontheedge.sql.format.Dbms;

import javax.sql.DataSource;


public interface DataSourceService
{
    DataSource getDataSource();

    Dbms getDbms();
}
