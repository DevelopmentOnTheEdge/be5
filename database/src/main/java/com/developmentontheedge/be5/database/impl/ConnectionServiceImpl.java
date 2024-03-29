package com.developmentontheedge.be5.database.impl;

import com.developmentontheedge.be5.database.ConnectionService;
import com.developmentontheedge.be5.database.DataSourceService;
import com.developmentontheedge.be5.database.RuntimeSqlException;
import com.developmentontheedge.be5.database.sql.TransactionExecutor;
import com.developmentontheedge.be5.database.sql.TransactionExecutorVoid;

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

    private final DataSourceService databaseService;

    @Inject
    public ConnectionServiceImpl(DataSourceService databaseService)
    {
        this.databaseService = databaseService;
    }

    Connection getCurrentTxConn()
    {
        return TRANSACT_CONN.get();
    }

    @Override
    public Connection beginTransaction()
    {
        Connection txConnection = getCurrentTxConn();
        if (txConnection == null)
        {
            TRANSACT_CONN_COUNT.set(1);
            return beginWorkWithTxConnection();
        }
        else
        {
            TRANSACT_CONN_COUNT.set(TRANSACT_CONN_COUNT.get() + 1);
            return txConnection;
        }
    }

    @Override
    public void endTransaction()
    {
        TRANSACT_CONN_COUNT.set(TRANSACT_CONN_COUNT.get() - 1);
        Connection txConnection = getCurrentTxConn();
        if (txConnection != null && TRANSACT_CONN_COUNT.get() == 0)
        {
            try
            {
                txConnection.commit();
            }
            catch (SQLException e)
            {
                throw new RuntimeSqlException(e);
            }
            finally
            {
                endWorkWithTxConnection();
            }
        }
    }

    @Override
    public void rollbackTransaction()
    {
        Connection txConnection = getCurrentTxConn();
        try
        {
            if (txConnection != null && !txConnection.isClosed())
            {
                txConnection.rollback();
            }
        }
        catch (SQLException e)
        {
            log.log(Level.SEVERE, "Unable to rollback transaction", e);
            throw new RuntimeSqlException(e);
        }
        finally
        {
            endWorkWithTxConnection();
        }
    }

    private Connection beginWorkWithTxConnection()
    {
        try
        {
            //log.log( Level.INFO, "beginWorkWithTxConnection", new Exception() );
            Connection conn = databaseService.getDataSource().getConnection();
            conn.setAutoCommit(false);
            TRANSACT_CONN.set(conn);
            return conn;
        }
        catch (SQLException e)
        {
            throw new RuntimeSqlException(e);
        }
    }

    private void endWorkWithTxConnection()
    {
        //log.log( Level.INFO, "endWorkWithTxConnection", new Exception() );
        returnConnection(getCurrentTxConn());
        TRANSACT_CONN.set(null);
    }

    @Override
    public <T> T inTransaction(TransactionExecutor<T> executor)
    {
        Connection conn;
        try
        {
            conn = beginTransaction();
            T res = executor.run(conn);
            endTransaction();
            return res;
        }
        catch (Throwable e)
        {
            rollbackTransaction();
            throw returnRuntimeExceptionOrWrap(e);
        }
    }

    @Override
    public void useTransaction(TransactionExecutorVoid executor)
    {
        inTransaction(getWrapperExecutor(executor));
    }

    private static TransactionExecutor<Void> getWrapperExecutor(final TransactionExecutorVoid voidExecutor)
    {
        return conn -> {
            voidExecutor.run(conn);
            return null;
        };
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        if (isInTransaction())
        {
            return getCurrentTxConn();
        }
        else
        {
            return databaseService.getDataSource().getConnection();
        }
    }

    @Override
    public void releaseConnection(Connection conn)
    {
        if (!isInTransaction())
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
                if (!conn.isClosed())
                {
                    conn.setAutoCommit(true);

                    if (conn.isReadOnly())
                        conn.setReadOnly(false);

                    conn.close();
                }
            }
            catch (SQLException e)
            {
                throw new RuntimeSqlException(e);
            }
        }
    }

    public boolean isInTransaction()
    {
        return getCurrentTxConn() != null;
    }

}
