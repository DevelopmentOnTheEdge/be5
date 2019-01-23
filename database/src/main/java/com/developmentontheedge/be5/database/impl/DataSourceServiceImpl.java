package com.developmentontheedge.be5.database.impl;

import com.developmentontheedge.be5.database.DataSourceService;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.lifecycle.Start;
import com.developmentontheedge.be5.meta.ProjectProvider;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.sql.DatabaseUtils;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.sql.format.dbms.Dbms;
import org.apache.commons.dbcp.BasicDataSource;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataSourceServiceImpl implements DataSourceService
{
    private static final Logger log = Logger.getLogger(DataSourceServiceImpl.class.getName());

    private final ProjectProvider projectProvider;

    private DataSource dataSource;
    private String connectionUrl;
    private Rdbms type;

    @Inject
    public DataSourceServiceImpl(ProjectProvider projectProvider)
    {
        this.projectProvider = projectProvider;
    }

    @Override
    public DataSource getDataSource()
    {
        return dataSource;
    }

    @Override
    public Dbms getDbms()
    {
        return type.getDbms();
    }

    @Override
    public String getConnectionUrl()
    {
        return connectionUrl;
    }

    @Start(order = 10)
    public void start() throws Exception
    {
        Project project = projectProvider.get();

        try
        {
            InitialContext ic = new InitialContext();
            Context xmlContext = (Context) ic.lookup("java:comp/env");

            initDataSource(project, xmlContext);
            initRdbmsType();
        }
        catch (NamingException e)
        {
            BeConnectionProfile profile = project.getConnectionProfile();
            if (profile == null)
            {
                throw Be5Exception.internal("Connection profile is not configured. and NamingException: ", e);
            }

            type = profile.getRdbms();

            BasicDataSource bds = new BasicDataSource();
            if (Rdbms.MYSQL != profile.getRdbms())
            {
                bds.setDriverClassName(profile.getDriverDefinition());
            }
            connectionUrl = profile.getJdbcUrl().createConnectionUrl(false);
            bds.setUrl(connectionUrl);
            String userName = profile.getUsername();
            bds.setUsername(userName);
            bds.setPassword(profile.getPassword());
            bds.setValidationQuery(type.getValidationQuery());

            dataSource = bds;
            log.info("Connection profile - " + profile.getName() + ". " +
                    "Connection url: " + DatabaseUtils.formatUrl(connectionUrl, userName, "xxxxx"));
        }

        project.setDatabaseSystem(type);
        projectProvider.addToReload(() -> project.setDatabaseSystem(type));
    }

    private void initDataSource(Project project, Context xmlContext)
    {
        try
        {
            String name = "jdbc/" + project.getAppName();
            dataSource = (DataSource) xmlContext.lookup(name);
            log.info("Context Configuration (context.xml): " + "'" + name + "'");
        }
        catch (Throwable e)
        {
            throw Be5Exception.internal("Error on init datasource", e);
        }
    }

    private void initRdbmsType()
    {
        Connection conn = null;
        try
        {
            conn = dataSource.getConnection();
            connectionUrl = conn.getMetaData().getURL();
            String userName = conn.getMetaData().getUserName();
            type = Rdbms.getRdbms(connectionUrl);
            log.info("Connection url: " + DatabaseUtils.formatUrl(connectionUrl, userName, "xxxxx"));
        }
        catch (Throwable e)
        {
            throw Be5Exception.internal("Error on init datasource", e);
        }
        finally
        {
            if (conn != null)
            {
                try
                {
                    conn.close();
                }
                catch (SQLException e)
                {
                    log.log(Level.SEVERE, "When close conn after fetching datasource", e);
                }
            }
        }
    }
}
