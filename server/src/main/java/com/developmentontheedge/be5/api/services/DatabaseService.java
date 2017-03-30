package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.sql.SqlExecutor;
import com.developmentontheedge.be5.metadata.sql.Rdbms;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface DatabaseService
{
    ResultSet executeQuery(String sql, boolean isReadOnly);

    Connection getConnection(boolean isReadOnly) throws SQLException;

    void close(Connection conn);

    void close(ResultSet rs);

    Connection getCurrentTxConn();

    <T> T transaction(SqlExecutor<T> executor);

    int getNumIdle();

    int getNumActive();

    String getConnectionsStatistics();

    Rdbms getRdbms();
}
