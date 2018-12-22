package com.developmentontheedge.be5.database;

import com.developmentontheedge.be5.base.Service;
import com.developmentontheedge.sql.format.dbms.Dbms;

import javax.sql.DataSource;

public interface DataSourceService extends Service
{
    DataSource getDataSource();

    Dbms getDbms();

    String getConnectionUrl();
}
