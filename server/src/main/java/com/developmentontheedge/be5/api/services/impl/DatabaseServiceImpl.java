package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.dbms.DbmsConnector;
import com.developmentontheedge.dbms.SimpleConnector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseServiceImpl implements DatabaseService
{
    public static Logger log = Logger.getLogger(DatabaseServiceImpl.class.getName());

    private ProjectProviderImpl projectProvider;

    public DatabaseServiceImpl(ProjectProviderImpl projectProvider){
        this.projectProvider = projectProvider;
    }

    @Override
    public DbmsConnector getDbmsConnector()
    {
        try
        {
            BeConnectionProfile profile = projectProvider.getProject().getConnectionProfile();
            try
            {
                //TODO
                Context initContext = new InitialContext();
                Context envContext = (Context) initContext.lookup("java:comp/env");
                DataSource ds = (DataSource) envContext.lookup("jdbc/UsersDB");
                Connection conn = ds.getConnection();
                return new SimpleConnector(profile.getRdbms().getType(), profile.getConnectionUrl(), conn);
            } catch (NamingException e)
            {
                e.printStackTrace();
                return null;
            }
//            return new SimpleConnector(profile.getRdbms().getType(), profile.getConnectionUrl(),
//                    profile.getUsername(), profile.getPassword());
        }
        catch (SQLException e)
        {
            log.log(Level.SEVERE, "Not create SimpleConnector", e);
            return null;
        }
    }

}
