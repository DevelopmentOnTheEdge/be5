package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.sql.SqlExecutor;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.dbms.DbmsConnector;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface DatabaseService extends DbmsConnector
{
    ResultSet executeQuery(String sql, boolean isReadOnly);

    Connection getConnection(boolean isReadOnly) throws SQLException;

    //void returnConnection(Connection conn);

    Connection getCurrentTxConn();

    <T> T transaction(SqlExecutor<T> executor);

    int getNumIdle();

    int getNumActive();

    String getConnectionsStatistics();

    Rdbms getRdbms();
}
