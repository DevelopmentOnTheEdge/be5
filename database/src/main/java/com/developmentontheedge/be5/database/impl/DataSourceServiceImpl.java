package com.developmentontheedge.be5.database.impl;

import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.lifecycle.Start;
import com.developmentontheedge.be5.meta.ProjectProvider;
import com.developmentontheedge.be5.database.DataSourceService;
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
        String configInfo;

        Connection conn = null;
        String userName;

        try
        {
            String name = "jdbc/" + project.getAppName();
            InitialContext ic = new InitialContext();
            Context xmlContext = (Context) ic.lookup("java:comp/env");
            dataSource = (DataSource) xmlContext.lookup(name);

            conn = dataSource.getConnection();
            connectionUrl = conn.getMetaData().getURL();
            userName = conn.getMetaData().getUserName();

            type = Rdbms.getRdbms(connectionUrl);

            configInfo = "Context Configuration (context.xml): " + "'" + name + "'";
        }
        catch (SQLException e)
        {
            throw Be5Exception.internal("When fetching datasource", e);
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
            userName = profile.getUsername();
            bds.setUsername(userName);
            bds.setPassword(profile.getPassword());

            dataSource = bds;
            configInfo = "Connection profile - " + profile.getName();
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
                    throw Be5Exception.internal("When close conn after fetching datasource", e);
                }
            }
        }

        project.setDatabaseSystem(type);
        projectProvider.addToReload(() -> project.setDatabaseSystem(type));

        log.info(configInfo);
        log.info("Using connection: " + DatabaseUtils.formatUrl(connectionUrl, userName, "xxxxx"));
    }
}
