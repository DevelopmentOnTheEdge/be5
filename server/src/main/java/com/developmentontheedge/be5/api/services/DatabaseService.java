package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.sql.SqlExecutor;
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

    Connection getCurrentTxConn();

    <T> T transaction(SqlExecutor<T> executor);

    int getNumIdle();

    int getNumActive();

    String getConnectionsStatistics();
}
