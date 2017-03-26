package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.dbms.DbmsConnector;
import com.developmentontheedge.dbms.DbmsType;
import com.developmentontheedge.dbms.SimpleConnector;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.naming.Context;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.api.exceptions.ExceptionHelper.getInternalBe5Exception;

class DatabaseServiceImpl implements DatabaseService
{
    private static final Logger log = Logger.getLogger(DatabaseServiceImpl.class.getName());

    private ProjectProvider projectProvider;
    private BasicDataSource bds = null;

    public DatabaseServiceImpl(ProjectProvider projectProvider){
        this.projectProvider = projectProvider;

        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");

        BeConnectionProfile profile = projectProvider.getProject().getConnectionProfile();
        Rdbms rdbms = profile.getRdbms();
        DbmsType type = rdbms.getType();

        bds = new BasicDataSource();
        bds.setDriverClassName(profile.getDriverDefinition());
        bds.setUrl(profile.getConnectionUrl());
        bds.setUsername(profile.getUsername());
        bds.setPassword(profile.getPassword());
    }

    @Override
    public DbmsConnector getDbmsConnector()
    {
        BeConnectionProfile profile = projectProvider.getProject().getConnectionProfile();

        try
        {
            return new SimpleConnector(profile.getRdbms().getType(), profile.getConnectionUrl(), getConnection(false));
        }
        catch (SQLException e)
        {
            throw getInternalBe5Exception(log, e);
        }
    }

    public DataSource getDataSource() {
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

}
