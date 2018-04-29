package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.sql.SqlExecutor;
import com.developmentontheedge.be5.api.sql.SqlExecutorVoid;
import com.developmentontheedge.sql.format.Dbms;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;


public interface DatabaseService
{
    String getConnectString();

    Connection getConnection(boolean isReadOnly) throws SQLException;

    Connection getCurrentTxConn();

    <T> T transactionWithResult(SqlExecutor<T> executor);

    void transaction(SqlExecutorVoid executor);

    void releaseConnection( Connection conn );

    RuntimeException rollback(Connection conn, Throwable e);

    Dbms getDbms();

    String getConnectionProfileName();

    String getUsername();

    Map<String, String> getParameters();
}
