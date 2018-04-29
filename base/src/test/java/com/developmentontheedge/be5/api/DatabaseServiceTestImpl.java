package com.developmentontheedge.be5.api;

import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.sql.format.Dbms;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;


public class DatabaseServiceTestImpl implements DatabaseService
{
    private static final Logger log = Logger.getLogger(DatabaseServiceTestImpl.class.getName());

    //Thread local?
    //private final Map<ResultSet, Connection> queriesMap = new ConcurrentHashMap<>(100);

    private DataSource dataSource;
    //private Rdbms type;

    public DatabaseServiceTestImpl()
    {
//        Project project = projectProvider.getProject();
//        String configInfo;
//        try
//        {
//            InitialContext ic = new InitialContext();
//            Context xmlContext = (Context) ic.lookup("java:comp/env");
//            dataSource = (DataSource) xmlContext.lookup("jdbc/" + project.getAppName());
//
//            String url = getConnectString();
//            type = Rdbms.getRdbms(url);
//
//            configInfo = "xml context : " + "'jdbc/" + project.getAppName() + "'";
//        }
//        catch (NamingException ignore)
//        {
//            BeConnectionProfile profile = project.getConnectionProfile();
//            if(profile == null)
//            {
//                throw Be5Exception.internal("Connection profile is not configured.");
//            }
//
//            type = profile.getRdbms();
//
//            BasicDataSource bds = new BasicDataSource();
//            if(Rdbms.MYSQL != profile.getRdbms())
//            {
//                bds.setDriverClassName(profile.getDriverDefinition());
//            }
//            bds.setUrl(profile.getConnectionUrl());
//            bds.setUsername(profile.getUsername());
//            bds.setPassword(profile.getPassword());
//
//            dataSource = bds;
//            configInfo = "connection profile form 'profile.local' - " + profile.getName();
//        }
//
//        project.setDatabaseSystem(type);
//        projectProvider.addToReload(() -> project.setDatabaseSystem(type));
//
//        log.info(JULLogger.infoBlock(
//            "ConfigInfo: " + configInfo +
//            "\nUsing connection:   " + DatabaseUtils.formatUrl(getConnectString(), getUsername(), "xxxxx")
//        ));
    }

    private DataSource getDataSource()
    {
        return dataSource;
    }

    @Override
    public Dbms getDbms()
    {
        return Dbms.H2;
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        return dataSource.getConnection();
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
