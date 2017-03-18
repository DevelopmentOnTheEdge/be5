package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.dbms.DbmsConnector;
import com.developmentontheedge.dbms.SimpleConnector;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseServiceImpl implements DatabaseService
{
    public static Logger log = Logger.getLogger(DatabaseServiceImpl.class.getName());

    public final String dataSourceName = "Be5dc";
    private ProjectProvider projectProvider;
    private BasicDataSource ds = null;

    public DatabaseServiceImpl(ProjectProvider projectProvider){
        this.projectProvider = projectProvider;
        constructBasicDataSource(projectProvider.getProject());
    }

    private void constructBasicDataSource(Project project) {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");

        try {
            BeConnectionProfile profile = project.getConnectionProfile();

            // constructBasicDataSource
            InitialContext ic = new InitialContext();
            BasicDataSource bds = new BasicDataSource();

            bds.setDriverClassName(profile.getDriverDefinition());
            bds.setUrl(profile.getConnectionUrl());
            bds.setUsername(profile.getUsername());
            bds.setPassword(profile.getPassword());

            ic.rebind(dataSourceName, bds);
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DbmsConnector getDbmsConnector()
    {
        try
        {
            BeConnectionProfile profile = projectProvider.getProject().getConnectionProfile();
            Connection connection = getConnection();
            if(connection != null)
            {
                return new SimpleConnector(profile.getRdbms().getType(), profile.getConnectionUrl(), connection);
            }
        }
        catch (SQLException e)
        {
            log.log(Level.SEVERE, "Not create SimpleConnector", e);
        }
        return null;
    }

    private Connection getConnection() {

        try {
            InitialContext ic = new InitialContext();
            ds = (BasicDataSource) ic.lookup(dataSourceName);
        } catch (NamingException e) {
            e.printStackTrace();
        }

        try {
            return ds != null ? ds.getConnection() : null;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int getNumIdle()
    {
        return ds != null ? ds.getNumIdle() : Integer.MAX_VALUE;
    }

    public int getNumActive()
    {
        return ds != null ? ds.getNumActive() : Integer.MAX_VALUE;
    }

    public String getConnectionsStatistics(){
        return "Active:" + getNumActive() + ", Idle:" + getNumIdle();
    }
}
