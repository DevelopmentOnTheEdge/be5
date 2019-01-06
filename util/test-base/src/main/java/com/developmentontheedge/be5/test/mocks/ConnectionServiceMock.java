package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.database.ConnectionService;
import com.developmentontheedge.be5.database.sql.TransactionExecutor;
import com.developmentontheedge.be5.database.sql.TransactionExecutorVoid;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionServiceMock implements ConnectionService
{
    @Override
    public Connection getConnection() throws SQLException
    {
        return null;
    }

    @Override
    public <T> T transactionWithResult(TransactionExecutor<T> executor)
    {
        try
        {
            return executor.run(null);
        }
        catch (Throwable e)
        {
            throw returnRuntimeExceptionOrWrap(e);
        }
    }

    @Override
    public void transaction(TransactionExecutorVoid executor)
    {
        try
        {
            executor.run(null);
        }
        catch (Throwable e)
        {
            throw returnRuntimeExceptionOrWrap(e);
        }
    }

    @Override
    public void releaseConnection(Connection conn)
    {

    }

    @Override
    public Connection beginTransaction()
    {
        return null;
    }

    @Override
    public void endTransaction()
    {

    }

    @Override
    public void rollbackTransaction()
    {

    }
}
