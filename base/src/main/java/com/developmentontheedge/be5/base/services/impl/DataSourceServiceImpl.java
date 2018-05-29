package com.developmentontheedge.be5.base.services.impl;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.database.DataSourceService;
import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.sql.DatabaseUtils;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import com.developmentontheedge.sql.format.Dbms;
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

    private DataSource dataSource;
    private Rdbms type;

    @Inject
    public DataSourceServiceImpl(ProjectProvider projectProvider)
    {
        Project project = projectProvider.getProject();
        String configInfo;

        Connection conn = null;
        String connectUrl;
        String userName;

        try
        {
            InitialContext ic = new InitialContext();
            Context xmlContext = (Context) ic.lookup("java:comp/env");
            dataSource = (DataSource) xmlContext.lookup("jdbc/" + project.getAppName());

            conn = dataSource.getConnection();
            connectUrl = conn.getMetaData().getURL();
            userName = conn.getMetaData().getUserName();

            type = Rdbms.getRdbms(connectUrl);

            configInfo = "xml context : " + "'jdbc/" + project.getAppName() + "'";
        }
        catch ( SQLException e )
        {
            throw Be5Exception.internal(e, "When fetching datasource");
        }
        catch (NamingException ignore)
        {
            BeConnectionProfile profile = project.getConnectionProfile();
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
            connectUrl = profile.getConnectionUrl();
            bds.setUrl(connectUrl);
            userName = profile.getUsername();
            bds.setUsername(userName);
            bds.setPassword(profile.getPassword());

            dataSource = bds;
            configInfo = "connection profile form 'profile.local' - " + profile.getName();
        }
        finally
        {
            if ( conn != null )
            {
                try
                {
                    conn.close();
                } catch (SQLException e)
                {
                    throw Be5Exception.internal(e, "When close conn after fetching datasource");
                }
            }
        }

        project.setDatabaseSystem(type);
        projectProvider.addToReload(() -> project.setDatabaseSystem(type));

        log.info(JULLogger.infoBlock(
            "ConfigInfo: " + configInfo +
            "\nUsing connection:   " + DatabaseUtils.formatUrl(connectUrl, userName, "xxxxx")
        ));
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
}
