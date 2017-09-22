package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.api.sql.SqlExecutor;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.sql.DatabaseUtils;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import com.developmentontheedge.dbms.DbmsType;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DatabaseServiceImpl implements DatabaseService
{
    private static final Logger log = Logger.getLogger(DatabaseServiceImpl.class.getName());

    //Thread local?
    private final Map<ResultSet, Connection> queriesMap = new ConcurrentHashMap<>(100);

    private static final ThreadLocal<Connection> TRANSACT_CONN = new ThreadLocal<>();

    private BasicDataSource bds = null;
    private Rdbms type;
    private BeConnectionProfile profile;

    public DatabaseServiceImpl(ProjectProvider projectProvider)
    {
        profile = projectProvider.getProject().getConnectionProfile();
        type = profile.getRdbms();

        bds = new BasicDataSource();
        bds.setDriverClassName(profile.getDriverDefinition());
        bds.setUrl(profile.getConnectionUrl());
        bds.setUsername(profile.getUsername());
        bds.setPassword(profile.getPassword());
//        //TODO add to Rdbms
//        bds.setValidationQuery("select 1");

        log.info(JULLogger.infoBlock(
              "Connection profile: " + getConnectionProfileName() +
            "\nUsing connection:   " + DatabaseUtils.formatUrl(getConnectString(), getUsername(), "xxxxx")
        ));
    }

    private DataSource getDataSource()
    {
        return bds;
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        return getConnection(false);
    }

    public Connection getConnection(boolean isReadOnly) throws SQLException
    {
        Connection conn = getDataSource().getConnection();
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
            catch (SQLException e) {
                throw Be5Exception.internal(log, e);
            }
        }
    }

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
            throw Be5Exception.internal(log, t, "get Statement" );
        }
        try
        {
            rs.close();
        }
        catch ( Throwable t )
        {
            throw Be5Exception.internal(log, t, "closing ResultSet" );
        }
        try
        {
            if ( stmt != null )
                stmt.close();
        }
        catch ( Throwable t )
        {
            throw Be5Exception.internal(log, t, "closing Statement" );
        }
        try
        {
            Connection conn = queriesMap.remove( rs );
            if ( conn != null && !isInTransaction() )
            {
                releaseConnection(conn);
            }
        }
        catch ( Throwable t )
        {
            throw Be5Exception.internal(log, t, "closing Connection" );
        }
    }

    public Connection getCurrentTxConn() {
        return TRANSACT_CONN.get();
    }

    private Connection getTxConnection() throws SQLException
    {
        Connection conn = TRANSACT_CONN.get();
        if (conn != null) {
            throw Be5Exception.internal(log, "Start second transaction in one thread");
        }
        conn = getDataSource().getConnection();
        conn.setAutoCommit(false);
        TRANSACT_CONN.set(conn);
        return conn;
    }

    private void closeTx(Connection conn) {
        returnConnection(conn);
        TRANSACT_CONN.set(null);
    }

    public <T> T transaction(SqlExecutor<T> executor)
    {
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

    private Be5Exception rollback(Connection conn, Throwable e)
    {
        try {
            if (conn != null) {
                conn.rollback();
            }
            return Be5Exception.internal(log, e);
        } catch (SQLException se) {
            return Be5Exception.internal(log, se, "Unable to rollback transaction", e);
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
    public DbmsType getType()
    {
        return getRdbms().getType();
    }

    @Override
    public String getConnectionProfileName()
    {
        return profile.getName();
    }

    @Override
    public String getConnectString()
    {
        return profile.getConnectionUrl();
    }

    @Override
    public String getUsername()
    {
        return profile.getUsername();
    }

    @Override
    public int executeUpdate(String query) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String executeInsert(String sql) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet executeQuery(String sql)
    {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try
        {
            conn = getConnection(true);
            stmt = conn.createStatement();
            rs = paranoidQuery(stmt, sql);
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            if( rs == null )
            {
                try
                {
                    if(stmt != null)stmt.close();
                } catch (SQLException e)
                {
                    log.log(Level.SEVERE, e.getMessage(), e);
                }
                if ( !isInTransaction() )
                {
                    returnConnection(conn);
                }
            }
            else if ( !isInTransaction() )
            {
                queriesMap.put( rs, conn );
            }
        }

        return rs;
    }

    private boolean isInTransaction()
    {
        return getCurrentTxConn() != null;
    }

    private ResultSet paranoidQuery(Statement stmt, String query)
    {
        String hacked = query;
        hacked += "/* STARTED: " + new java.sql.Timestamp( System.currentTimeMillis() ) + " */";

        try
        {
            return stmt.executeQuery( hacked );
        }
        catch( SQLException e )
        {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> getParameters()
    {
        Map<String, String> map = new TreeMap<>();
        map.put("Active/Idle", bds.getNumActive() + " / " + bds.getNumIdle());
        map.put("Username", bds.getUsername());
        map.put("DefaultCatalog", bds.getDefaultCatalog());
        map.put("DriverClassName", bds.getDriverClassName());
        map.put("Url", bds.getUrl());
        map.put("JmxName", bds.getJmxName());
        map.put("ValidationQuery", bds.getValidationQuery());
        map.put("EvictionPolicyClassName", bds.getEvictionPolicyClassName());
        map.put("ConnectionInitSqls", bds.getConnectionInitSqls().toString());

        return map;
    }

}
