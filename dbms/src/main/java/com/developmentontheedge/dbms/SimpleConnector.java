package com.developmentontheedge.dbms;

import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleConnector implements DbmsConnector
{
    private static final Logger log = Logger.getLogger(SimpleConnector.class.getName());

    private final String connectionUrl;
    private final DbmsType type;
    private final DataSource dataSource;

    public SimpleConnector(DbmsType type, String connectionUrl, String username, String password)
    {
        BasicDataSource bds = new BasicDataSource();

        //bds.setDriverClassName(profile.getDriverDefinition());
        bds.setUrl(connectionUrl);
        bds.setUsername(username);
        bds.setPassword(password);

        this.type = type;
        this.connectionUrl = connectionUrl;

        dataSource = bds;
//        try
//        {
//            this.connection = DriverManager.getConnection( connectionUrl, username, password );
//        }
//        catch (SQLException e)
//        {
//            throw propagate(e);
//        }
    }

    public SimpleConnector(DbmsType type, String connectionUrl, DataSource dataSource)
    {
        this.type = type;
        this.connectionUrl = connectionUrl;
        this.dataSource = dataSource;
    }

    @Override
    public DbmsType getType()
    {
        return type;
    }

    @Override
    public String getConnectString()
    {
        return connectionUrl;
    }

    @Override
    public int executeUpdate( String query ) throws SQLException
    {
        try(Statement st = getConnection().createStatement())
        {
            return st.executeUpdate( query );
        }
    }

    @Override
    public ResultSet executeQuery( String sql ) throws SQLException
    {
        return getConnection().createStatement().executeQuery(sql);
    }

    @Override
    public String executeInsert( String sql ) throws SQLException
    {
        try(Statement st = getConnection().createStatement())
        {
            st.execute(sql);
        }
        // TODO support return of insert key
        return null;
    }

    @Override
    public void close( ResultSet rs )
    {
        if(rs == null)
            return;
        Statement st = null;
        try
        {
            st = rs.getStatement();
        }
        catch( SQLException e1 )
        {
        }
        try
        {
            rs.close();
        }
        catch ( SQLException e )
        {
        }
        try
        {
            if(st != null)
                st.close();
        }
        catch ( SQLException e )
        {
        }
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        return dataSource.getConnection();
    }

    private void returnConnection(Connection conn)
    {
        try
        {
            if(!conn.isClosed())
            {
                if(!conn.getAutoCommit())
                    conn.setAutoCommit(true);
                conn.close();
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void releaseConnection( Connection conn )
    {
        if ( null == conn )
        {
            return;
        }

        returnConnection(conn);
    }

    private RuntimeException propagate(SQLException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
        return new RuntimeException(e);
    }
}
