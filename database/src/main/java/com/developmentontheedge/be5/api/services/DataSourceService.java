package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.sql.format.Dbms;

import javax.sql.DataSource;


public interface DataSourceService
{
    DataSource getDataSource();

    Dbms getDbms();
}
