package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.dbms.DbmsConnector;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseService
{
    DataSource getDataSource();

    DbmsConnector getDbmsConnector();

    Connection getConnection(boolean isReadOnly) throws SQLException;

    void close(Connection conn);

    int getNumIdle();

    int getNumActive();

    String getConnectionsStatistics();
}
