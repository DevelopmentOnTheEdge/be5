package com.developmentontheedge.be5.database.impl;

import com.developmentontheedge.be5.database.ConnectionService;
import com.developmentontheedge.be5.database.DataSourceService;
import com.developmentontheedge.be5.database.sql.SqlExecutor;
import com.developmentontheedge.be5.database.sql.SqlExecutorVoid;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ConnectionServiceImpl implements ConnectionService
{
    private static final ThreadLocal<Connection> TRANSACT_CONN = new ThreadLocal<>();
    private static final ThreadLocal<Integer> TRANSACT_CONN_COUNT = new ThreadLocal<>();

    private static final Logger log = Logger.getLogger(ConnectionServiceImpl.class.getName());

    private DataSourceService databaseService;

    @Inject
    public ConnectionServiceImpl(DataSourceService databaseService)
    {
        this.databaseService = databaseService;
    }

    public Connection getCurrentTxConn()
    {
        return TRANSACT_CONN.get();
    }

    private Connection getTxConnection() throws SQLException
    {
        Connection conn = TRANSACT_CONN.get();
        if (conn != null)
        {
            return conn;
        }
        else
        {
            conn = databaseService.getDataSource().getConnection();
            conn.setAutoCommit(false);
            TRANSACT_CONN.set(conn);
            return conn;
        }
    }

    private void closeTx(Connection conn)
    {
        returnConnection(conn);
        TRANSACT_CONN.set(null);
    }

    @Override
    public <T> T transactionWithResult(SqlExecutor<T> executor)
    {
        Connection conn = null;
        try {
            conn = getTxConnection();

            if(TRANSACT_CONN_COUNT.get() == null)TRANSACT_CONN_COUNT.set(0);

            TRANSACT_CONN_COUNT.set(TRANSACT_CONN_COUNT.get() + 1);
            T res = executor.run(conn);
            TRANSACT_CONN_COUNT.set(TRANSACT_CONN_COUNT.get() - 1);

            if(TRANSACT_CONN_COUNT.get() == 0)
            {
                conn.commit();
            }

            return res;
        } catch (Error | Exception e) {
            TRANSACT_CONN_COUNT.set(0);
            throw rollback(conn, e);
        } finally {
            if(TRANSACT_CONN_COUNT.get() == 0)
            {
                closeTx(conn);
            }
        }
    }

    @Override
    public void transaction(SqlExecutorVoid executor)
    {
        transactionWithResult(getWrapperExecutor(executor));
    }

    private static SqlExecutor<Void> getWrapperExecutor(final SqlExecutorVoid voidExecutor) {
        return conn -> {
            voidExecutor.run(conn);
            return null;
        };
    }

    @Override
    public RuntimeException rollback(Connection conn, Throwable e)
    {
        try
        {
            if (conn != null && !conn.isClosed())
            {
                conn.rollback();
            }

            return new RuntimeException(e);
        }
        catch (SQLException se)
        {
            log.log(Level.SEVERE, "Unable to rollback transaction", se);
            return new RuntimeException(e);
        }
    }

    @Override
    public Connection getConnection(boolean isReadOnly) throws SQLException
    {
        Connection conn = databaseService.getDataSource().getConnection();
        if (isReadOnly) {
            conn.setReadOnly(true);
        }
        return conn;
    }

    @Override
    public void releaseConnection(Connection conn)
    {
        if ( !isInTransaction() )
        {
            returnConnection(conn);
        }
    }

    private void returnConnection(Connection conn)
    {
        if (conn != null)
        {
            try
            {
                if(!conn.isClosed())
                {
                    if(!conn.getAutoCommit())
                        conn.setAutoCommit(true);
                    if (conn.isReadOnly())
                        conn.setReadOnly(false);

                    conn.close();
                }
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isInTransaction()
    {
        return getCurrentTxConn() != null;
    }

}
