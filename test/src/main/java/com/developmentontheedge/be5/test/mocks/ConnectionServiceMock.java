package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.api.services.ConnectionService;
import com.developmentontheedge.be5.api.sql.SqlExecutor;
import com.developmentontheedge.be5.api.sql.SqlExecutorVoid;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionServiceMock implements ConnectionService
{
    @Override
    public Connection getConnection(boolean isReadOnly) throws SQLException
    {
        return null;
    }

    @Override
    public Connection getCurrentTxConn()
    {
        return null;
    }

    @Override
    public <T> T transactionWithResult(SqlExecutor<T> executor)
    {
        try {
            return executor.run(null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void transaction(SqlExecutorVoid executor)
    {
        try {
            executor.run(null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void releaseConnection(Connection conn)
    {

    }

    @Override
    public RuntimeException rollback(Connection conn, Throwable e)
    {
        return null;
    }
}
