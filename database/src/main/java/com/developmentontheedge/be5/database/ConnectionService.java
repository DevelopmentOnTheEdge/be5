package com.developmentontheedge.be5.database;

import com.developmentontheedge.be5.database.sql.TransactionExecutor;
import com.developmentontheedge.be5.database.sql.TransactionExecutorVoid;

import java.sql.Connection;
import java.sql.SQLException;


public interface ConnectionService
{
    Connection getConnection() throws SQLException;

    Connection beginTransaction();

    void endTransaction();

    void rollbackTransaction();

    <T> T transactionWithResult(TransactionExecutor<T> executor);

    void transaction(TransactionExecutorVoid executor);

    void releaseConnection(java.sql.Connection conn);

    default RuntimeException returnRuntimeExceptionOrWrap(Throwable e)
    {
        return e instanceof RuntimeException ? (RuntimeException) e : new RuntimeSqlException("rethrow after rollback", e);
    }
}
