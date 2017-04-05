package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.api.sql.SqlExecutor;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.naming.Context;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.api.exceptions.ExceptionHelper.getInternalBe5Exception;

public class DatabaseServiceImpl implements DatabaseService
{
    private static final Logger log = Logger.getLogger(DatabaseServiceImpl.class.getName());

    private final static String MSG_ERROR_CLOSING = "Error closing result set";

    //Thread local?
    private final Map<ResultSet, Connection> queriesMap = new ConcurrentHashMap<>(100);

    private static final ThreadLocal<Connection> TRANSACT_CONN = new ThreadLocal<>();

    private BasicDataSource bds = null;
    private Rdbms type;

    public DatabaseServiceImpl(ProjectProvider projectProvider){
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");

        BeConnectionProfile profile = projectProvider.getProject().getConnectionProfile();
        type = profile.getRdbms();

        bds = new BasicDataSource();
        bds.setDriverClassName(profile.getDriverDefinition());
        bds.setUrl(profile.getConnectionUrl());
        bds.setUsername(profile.getUsername());
        bds.setPassword(profile.getPassword());
        //TODO add to Rdbms
        bds.setValidationQuery("select 1");
    }

    private DataSource getDataSource() {
        return bds;
    }

    public Connection getConnection(boolean isReadOnly) throws SQLException
    {
        Connection conn = getDataSource().getConnection();
        if (isReadOnly) {
            conn.setReadOnly(true);
        }
        return conn;
    }

    public void close(Connection conn)
    {
        if (conn != null)
        {
            try
            {
                if (conn.isReadOnly())
                {
                    conn.setReadOnly(false);
                }
                conn.close();
            }
            catch (SQLException e) {
                throw getInternalBe5Exception(log, e);
            }
        }
    }

    @Override
    public void close(ResultSet rs)
    {
        if ( rs == null )
            return;
        Statement stmt;
        try
        {
            stmt = rs.getStatement();
        }
        catch ( Throwable t )
        {
            throw getInternalBe5Exception(log, MSG_ERROR_CLOSING, t );
        }
        try
        {
            rs.close();
        }
        catch ( Throwable t )
        {
            throw getInternalBe5Exception(log, MSG_ERROR_CLOSING, t );
        }
        try
        {
            if ( stmt != null )
                stmt.close();
        }
        catch ( Throwable t )
        {
            throw getInternalBe5Exception(log, MSG_ERROR_CLOSING, t );
        }
        try
        {
            Connection conn = queriesMap.remove( rs );
            if ( conn != null && !inTransaction() )
            {
                close(conn);
            }
        }
        catch ( Throwable t )
        {
            throw getInternalBe5Exception(log, MSG_ERROR_CLOSING, t );
        }
    }

    public Connection getCurrentTxConn() {
        return TRANSACT_CONN.get();
    }

    private Connection getTxConnection() throws SQLException
    {
        Connection conn = TRANSACT_CONN.get();
        if (conn != null) {
            throw getInternalBe5Exception(log, "Start second transaction in one thread");
        }
        conn = getDataSource().getConnection();
        conn.setAutoCommit(false);
        TRANSACT_CONN.set(conn);
        return conn;
    }

    private void closeTx(Connection conn) {
        close(conn);
        TRANSACT_CONN.set(null);
    }

    public <T> T transaction(SqlExecutor<T> executor) {
        Connection conn = null;
        try {
            conn = getTxConnection();
            T res = executor.run(conn);
            conn.commit();
            return res;
        } catch (Error | Exception e) {
            throw rollback(conn, e);
        } finally {
            closeTx(conn);
        }
    }

    private Be5Exception rollback(Connection conn, Throwable e) {
        try {
            if (conn != null) {
                conn.rollback();
            }
            return getInternalBe5Exception(log, e);
        } catch (SQLException se) {
            return getInternalBe5Exception(log, "Unable to rollback transaction", e);
        }
    }

    public int getNumIdle()
    {
        return bds != null ? bds.getNumIdle() : Integer.MAX_VALUE;
    }

    public int getNumActive()
    {
        return bds != null ? bds.getNumActive() : Integer.MAX_VALUE;
    }

    public String getConnectionsStatistics(){
        return "Active:" + getNumActive() + ", Idle:" + getNumIdle();
    }

    @Override
    public Rdbms getRdbms()
    {
        return type;
    }

    @Override
    public ResultSet executeQuery(String sql, boolean isReadOnly)
    {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try
        {
            conn = getConnection(isReadOnly);
            stmt = conn.createStatement();
            rs = paranoidQuery(stmt, sql);
        }
        catch (SQLException e)
        {
            throw getInternalBe5Exception(log, e);
        }
        finally
        {
            if( rs == null )
            {
                try
                {
                    stmt.close();
                } catch (SQLException e)
                {
                    throw getInternalBe5Exception(log, e);
                }
                if ( !inTransaction() )
                {
                    close(conn);
                }
            }
            else if ( !inTransaction() )
            {
                queriesMap.put( rs, conn );
            }
        }

        return rs;
    }

    private boolean inTransaction()
    {
        return getCurrentTxConn() != null;
    }

    protected ResultSet paranoidQuery(Statement stmt, String query)
    {
        ResultSet rs = null;

        String hacked = query;
        hacked += "/* STARTED: " + new java.sql.Timestamp( System.currentTimeMillis() ) + " */";

        try
        {
            return stmt.executeQuery( hacked );
        }
        catch( SQLException e )
        {
            throw getInternalBe5Exception(log, e);
        }
    }

}
