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

    /**
     * Executes <code>executor</code> in a transaction, and returns the result of the callback.
     *
     * @param executor a callback which will receive an open connection, in a transaction.
     * @param <T> type returned by callback
     * @return value returned from the executor
     */
    <T> T inTransaction(TransactionExecutor<T> executor);

    /**
     * Executes <code>executor</code> in a transaction.
     *
     * @param executor a callback which will receive an open connection, in a transaction.
     */
    void useTransaction(TransactionExecutorVoid executor);

    /**
     * @return whether the connector is in a transaction.
     */
    boolean isInTransaction();

    void releaseConnection(java.sql.Connection conn);

    default RuntimeException returnRuntimeExceptionOrWrap(Throwable e)
    {
        return e instanceof RuntimeException ? (RuntimeException) e : new RuntimeSqlException("rethrow after rollback", e);
    }
}
