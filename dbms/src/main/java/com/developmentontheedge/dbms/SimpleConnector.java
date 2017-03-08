package com.developmentontheedge.dbms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SimpleConnector implements DbmsConnector
{
    private final String connectionUrl;
    private final DbmsType type;
    private final Connection connection;

    public SimpleConnector(DbmsType type, String connectionUrl, String username, String password) throws SQLException
    {
        this.type = type;
        this.connectionUrl = connectionUrl;
        this.connection = DriverManager.getConnection( connectionUrl, username, password );
    }

    public SimpleConnector(DbmsType type, String connectionUrl, Connection connection) throws SQLException
    {
        this.type = type;
        this.connectionUrl = connectionUrl;
        this.connection = connection;
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
        try(Statement st = connection.createStatement())
        {
            return st.executeUpdate( query );
        }
    }

    @Override
    public ResultSet executeQuery( String sql ) throws SQLException
    {
        return connection.createStatement().executeQuery( sql );
    }

    @Override
    public String executeInsert( String sql ) throws SQLException
    {
        try(Statement st = connection.createStatement())
        {
            st.execute( sql );
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
        return connection;
    }

    @Override
    public void releaseConnection( Connection conn ) throws SQLException
    {
    }
//TODO
    @Override
    public boolean isMySQL() {
        return false;
    }

    @Override
    public boolean isMySQL5() {
        return false;
    }

    @Override
    public boolean isMySQL41() {
        return false;
    }

    @Override
    public boolean isODBC() {
        return false;
    }

    @Override
    public boolean isSQLite() {
        return false;
    }

    @Override
    public boolean isOracle() {
        return false;
    }

    @Override
    public boolean isOracle8() {
        return false;
    }

    @Override
    public boolean isSQLServer() {
        return false;
    }

    @Override
    public boolean isSQLServer2005() {
        return false;
    }

    @Override
    public boolean isSQLServerJTDS() {
        return false;
    }

    @Override
    public boolean isDb2() {
        return false;
    }

    @Override
    public boolean isDb2NetDriver() {
        return false;
    }

    @Override
    public boolean isDb2AppDriver() {
        return false;
    }

    @Override
    public boolean isDb2v8() {
        return false;
    }

    @Override
    public boolean isDb2v9() {
        return false;
    }

    @Override
    public boolean isPostgreSQL() {
        return false;
    }
}
