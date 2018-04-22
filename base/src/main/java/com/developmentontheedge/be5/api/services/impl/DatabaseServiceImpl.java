package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.api.sql.SqlExecutor;
import com.developmentontheedge.be5.api.sql.SqlExecutorVoid;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.sql.DatabaseUtils;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import com.developmentontheedge.dbms.DbmsType;
import org.apache.commons.dbcp.BasicDataSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;


public class DatabaseServiceImpl implements DatabaseService
{
    private static final Logger log = Logger.getLogger(DatabaseServiceImpl.class.getName());

    //Thread local?
    //private final Map<ResultSet, Connection> queriesMap = new ConcurrentHashMap<>(100);

    private static final ThreadLocal<Connection> TRANSACT_CONN = new ThreadLocal<>();
    private static final ThreadLocal<Integer> TRANSACT_CONN_COUNT = new ThreadLocal<>();

    private DataSource dataSource = null;
    private Rdbms type;
    private BeConnectionProfile profile = null;

    public DatabaseServiceImpl(ProjectProvider projectProvider)
    {
        Project project = projectProvider.getProject();
        String configInfo;
        try
        {
            InitialContext ic = new InitialContext();
            Context xmlContext = (Context) ic.lookup("java:comp/env");
            dataSource = (DataSource) xmlContext.lookup("jdbc/" + project.getAppName());

            String url = getConnectString();
            type = Rdbms.getRdbms(url);

            configInfo = "xml context : " + "'jdbc/" + project.getAppName() + "'";
        }
        catch (NamingException ignore)
        {
            profile = project.getConnectionProfile();
            if(profile == null)
            {
                throw Be5Exception.internal("Connection profile is not configured.");
            }

            type = profile.getRdbms();

            BasicDataSource bds = new BasicDataSource();
            if(Rdbms.MYSQL != profile.getRdbms())
            {
                bds.setDriverClassName(profile.getDriverDefinition());
            }
            bds.setUrl(profile.getConnectionUrl());
            bds.setUsername(profile.getUsername());
            bds.setPassword(profile.getPassword());

            dataSource = bds;
            configInfo = "connection profile form 'profile.local' - " + profile.getName();
        }

        project.setDatabaseSystem(getRdbms());

        log.info(JULLogger.infoBlock(
            "ConfigInfo: " + configInfo +
            "\nUsing connection:   " + DatabaseUtils.formatUrl(getConnectString(), getUsername(), "xxxxx")
        ));
    }

    private DataSource getDataSource()
    {
        return dataSource;
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

    public Connection getCurrentTxConn()
    {
        return TRANSACT_CONN.get();
    }

    private Connection getTxConnection() throws SQLException
    {
        Connection conn = TRANSACT_CONN.get();
        if (conn != null)
        {
            return conn;//for nested transactions
        }
        else
        {
            conn = getDataSource().getConnection();
            conn.setAutoCommit(false);
            TRANSACT_CONN.set(conn);
            return conn;
        }
    }

    private void closeTx(Connection conn) {
        returnConnection(conn);
        TRANSACT_CONN.set(null);
    }

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
    public Be5Exception rollback(Connection conn, Throwable e)
    {
        try
        {
            if (conn != null)
            {
                conn.rollback();
            }
            if(e instanceof Be5Exception)
            {
                return (Be5Exception)e;
            }
            else
            {
                return Be5Exception.internal(log, e);
            }
        }
        catch (SQLException se)
        {
            return Be5Exception.internal(log, se, "Unable to rollback transactionWithResult", e);
        }
    }

    @Override
    public Rdbms getRdbms()
    {
        return type;
    }

    @Override
    public String getConnectionProfileName()
    {
        return profile != null ? profile.getName() : null;
    }

    @Override
    public String getConnectString()
    {
        if(dataSource instanceof BasicDataSource)
        {
            return ((BasicDataSource) dataSource).getUrl();
        }
        throw Be5Exception.internal("Unknown dataSource");
    }

    @Override
    public String getUsername()
    {
        if(dataSource instanceof BasicDataSource)
        {
            return ((BasicDataSource) dataSource).getUsername();
        }
        throw Be5Exception.internal("Unknown dataSource");
    }

    private boolean isInTransaction()
    {
        return getCurrentTxConn() != null;
    }

    public Map<String, String> getParameters()
    {
        Map<String, String> map = new TreeMap<>();

        if(dataSource instanceof BasicDataSource)
        {
            BasicDataSource dataSource = (BasicDataSource)this.dataSource;
            map.put("DataSource class", dataSource.getClass().getCanonicalName());
            map.put("Active/Idle", dataSource.getNumActive() + " / " + dataSource.getNumIdle());
            map.put("max Active/max Idle", dataSource.getMaxActive() + " / " + dataSource.getMaxIdle());
            map.put("max wait", dataSource.getMaxWait() + "");
            map.put("Username", dataSource.getUsername());
            map.put("DefaultCatalog", dataSource.getDefaultCatalog());
            map.put("DriverClassName", dataSource.getDriverClassName());
            map.put("Url", dataSource.getUrl());
            //map.put("JmxName", dataSource.getJmxName());
            map.put("ValidationQuery", dataSource.getValidationQuery());
            //map.put("EvictionPolicyClassName", dataSource.getEvictionPolicyClassName());
            map.put("ConnectionInitSqls", dataSource.getConnectionInitSqls().toString());

        }

        return map;
    }

}
