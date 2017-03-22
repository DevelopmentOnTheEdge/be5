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

    public SimpleConnector(DbmsType type, String connectionUrl, Connection connection)
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

    private void returnConnection(Connection conn) throws SQLException
    {
        if(!conn.isClosed())
        {
            if(!conn.getAutoCommit())
                conn.setAutoCommit(true);
            conn.close();
        }
    }

    @Override
    public void releaseConnection( Connection conn ) throws SQLException
    {
        if ( null == conn )
        {
            return;
        }

        returnConnection(conn);
    }

    protected boolean isDBMS( String ... urlPrefixes )
    {
        if ( connectionUrl == null )
        {
            return false;
        }

        for( String urlPrefix : urlPrefixes )
        {
            if( connectionUrl.startsWith( urlPrefix ) )
            {
                return true;
            }
        }

        return false;
    }

    public boolean isODBC()
    {
        return isDBMS( "jdbc:odbc:" );
    }

    public boolean isJDBCDBF()
    {
        return isDBMS( "jdbc:dbf:" );
    }

    public boolean isSQLite()
    {
        return isDBMS( "jdbc:sqlite:" );
    }

    public boolean isMySQL()
    {
        return isDBMS( "jdbc:mysql:" );
    }

    @Override
    public boolean isMySQL5()
    {
        return false;
    }

    @Override
    public boolean isMySQL41()
    {
        return false;
    }

    public boolean isFirebird()
    {
        return isDBMS( "jdbc:firebirdsql:" );
    }

    public boolean isOracle()
    {
        return isDBMS( "jdbc:oracle:" );
    }

    @Override
    public boolean isOracle8()
    {
        return false;
    }

    public boolean isOracleViaUrlCheck()
    {
        return isDBMS( "jdbc:oracle:" );
    }

    public boolean isSQLServer()
    {
        return isDBMS( "jdbc:microsoft:sqlserver:", "jdbc:sqlserver:", "jdbc:jtds:sqlserver:" ) || "jdbc:sqljdbc://".equals( connectionUrl );
    }

    public boolean isSQLServer2005()
    {
        return isDBMS( "jdbc:sqlserver:", "jdbc:jtds:sqlserver:" ) || "jdbc:sqljdbc://".equals( connectionUrl );
    }

    public boolean isSQLServerJTDS()
    {
        return isDBMS( "jdbc:jtds:sqlserver:" );
    }

    protected boolean isSQLServerViaUrlCheck()
    {
        return isDBMS( "jdbc:microsoft:sqlserver:", "jdbc:sqlserver:", "jdbc:jtds:sqlserver:" ) || "jdbc:sqljdbc://".equals( connectionUrl );
    }

    public boolean isDb2()
    {
        return isDBMS( "jdbc:db2:" );
    }

    @Override
    public boolean isDb2NetDriver()
    {
        return false;
    }

    @Override
    public boolean isDb2AppDriver()
    {
        return false;
    }

    @Override
    public boolean isDb2v8()
    {
        return false;
    }

    @Override
    public boolean isDb2v9()
    {
        return false;
    }

    @Override
    public boolean isPostgreSQL()
    {
        return isDBMS( "jdbc:postgresql:" );
    }

    @Override
    public boolean isH2() {
        return isDBMS( "jdbc:h2:" );
    }

    public boolean isMSAccess()
    {
        return isDBMS( "jdbc:ucanaccess:" );
    }
}
