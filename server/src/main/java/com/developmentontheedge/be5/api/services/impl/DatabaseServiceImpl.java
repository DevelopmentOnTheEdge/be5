package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.ProjectProvider;
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
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;


public class DatabaseServiceImpl implements DatabaseService
{
    private static final Logger log = Logger.getLogger(DatabaseServiceImpl.class.getName());

    //Thread local?
    //private final Map<ResultSet, Connection> queriesMap = new ConcurrentHashMap<>(100);

    private DataSource dataSource;
    private Rdbms type;

    @Inject
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
            bds.setUrl(profile.getConnectionUrl());
            bds.setUsername(profile.getUsername());
            bds.setPassword(profile.getPassword());

            dataSource = bds;
            configInfo = "connection profile form 'profile.local' - " + profile.getName();
        }

        project.setDatabaseSystem(type);
        projectProvider.addToReload(() -> project.setDatabaseSystem(type));

        log.info(JULLogger.infoBlock(
            "ConfigInfo: " + configInfo +
            "\nUsing connection:   " + DatabaseUtils.formatUrl(getConnectString(), getUsername(), "xxxxx")
        ));
    }

    private DataSource getDataSource()
    {
        return dataSource;
    }

    @Override
    public Dbms getDbms()
    {
        return type.getDbms();
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        return dataSource.getConnection();
    }

    private String getConnectString()
    {
        if(dataSource instanceof BasicDataSource)
        {
            return ((BasicDataSource) dataSource).getUrl();
        }
        throw Be5Exception.internal("Unknown dataSource");
    }

    private String getUsername()
    {
        if(dataSource instanceof BasicDataSource)
        {
            return ((BasicDataSource) dataSource).getUsername();
        }
        throw Be5Exception.internal("Unknown dataSource");
    }

    @Override
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
