package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.dbms.DbmsConnector;
import com.developmentontheedge.dbms.SimpleConnector;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseServiceImpl implements DatabaseService
{
    public static Logger log = Logger.getLogger(DatabaseServiceImpl.class.getName());

    private ProjectProviderImpl projectProvider;
    private BasicDataSource ds = null;

    public DatabaseServiceImpl(ProjectProviderImpl projectProvider){
        this.projectProvider = projectProvider;
    }

    @Override
    public DbmsConnector getDbmsConnector()
    {
        try
        {
            BeConnectionProfile profile = projectProvider.getProject().getConnectionProfile();

            return new SimpleConnector(profile.getRdbms().getType(), profile.getConnectionUrl(),
                    getConnection());
        }
        catch (SQLException e)
        {
            log.log(Level.SEVERE, "Not create SimpleConnector", e);
            return null;
        }
    }

    private Connection getConnection() {


        try {
            InitialContext ic = new InitialContext();
            ds = (BasicDataSource) ic.lookup("jdbc/testBe5");
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
        return ds.getNumIdle();
    }

    public int getNumActive()
    {
        return ds.getNumActive();
    }

    public String getConnectionsStatistics(){
        return "Active:" + getNumActive() + ", Idle:" + getNumIdle();
    }
}
