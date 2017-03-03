package com.developmentontheedge.be5.metadata.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.beanexplorer.enterprise.DatabaseConnector;
import com.developmentontheedge.dbms.DbmsConnector;
import com.developmentontheedge.dbms.DbmsType;

public class BeanExplorerDbmsConnector implements DbmsConnector
{
    private final DatabaseConnector connector;

    public BeanExplorerDbmsConnector(DatabaseConnector connector)
    {
        this.connector = connector;
    }

    @Override
    public DbmsType getType()
    {
        if ( connector.isDb2() )
        {
            return DbmsType.DB2;
        }
        else if ( connector.isMySQL() )
        {
            return DbmsType.MYSQL;
        }
        else if ( connector.isOracle() )
        {
            return DbmsType.ORACLE;
        }
        else if ( connector.isSQLServer() )
        {
            return DbmsType.SQLSERVER;
        }
        else if ( connector.isPostgreSQL() )
        {
            return DbmsType.POSTGRESQL;
        }
        throw new IllegalStateException( "Unsupported connector: "+connector.getConnectString() );
    }

    @Override
    public String getConnectString()
    {
        String connectString = connector.getConnectString();
        try
        {
            ConnectionUrl url = new ConnectionUrl( connectString );
            if(url.getProperty( "password" ) != null)
            {
                url.setProperty( "password", "xxxxx" );
            }
            return url.toString();
        }
        catch ( IllegalArgumentException e )
        {
            // ignore
        }
        return connectString;
    }

    @Override
    public int executeUpdate( String query ) throws SQLException
    {
        return connector.executeUpdate( query );
    }

    @Override
    public ResultSet executeQuery( String sql ) throws SQLException
    {
        return connector.executeQuery( sql );
    }

    @Override
    public String executeInsert( String sql ) throws SQLException
    {
        return connector.executeInsert( sql );
    }

    @Override
    public void close( ResultSet rs )
    {
        connector.close( rs );
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        return connector.getConnection();
    }

    @Override
    public void releaseConnection( Connection conn ) throws SQLException
    {
        connector.releaseConnection( conn );
    }
}
