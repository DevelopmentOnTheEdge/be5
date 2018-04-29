package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.sql.SqlExecutor;
import com.developmentontheedge.be5.api.sql.SqlExecutorVoid;

import java.sql.Connection;
import java.sql.SQLException;


public interface ConnectionService
{
    Connection getConnection(boolean isReadOnly) throws SQLException;

    Connection getCurrentTxConn();

    <T> T transactionWithResult(SqlExecutor<T> executor);

    void transaction(SqlExecutorVoid executor);

    void releaseConnection( java.sql.Connection conn );

    RuntimeException rollback(java.sql.Connection conn, Throwable e);
}
