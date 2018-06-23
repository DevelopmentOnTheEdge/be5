package com.developmentontheedge.be5.database;

import com.developmentontheedge.be5.database.sql.SqlExecutor;
import com.developmentontheedge.be5.database.sql.SqlExecutorVoid;

import java.sql.Connection;
import java.sql.SQLException;


public interface ConnectionService
{
    Connection getConnection(boolean isReadOnly) throws SQLException;

    Connection getCurrentTxConn();

    Connection beginTransaction() throws SQLException;

    void endTransaction() throws SQLException;

    RuntimeException rollbackTransaction(Throwable e);

    <T> T transactionWithResult(SqlExecutor<T> executor);

    void transaction(SqlExecutorVoid executor);

    void releaseConnection( java.sql.Connection conn );
}
