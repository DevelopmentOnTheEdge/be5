package com.developmentontheedge.be5.metadata.sql.schema;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.beanexplorer.enterprise.DatabaseConnector;
import com.developmentontheedge.be5.metadata.sql.BeSqlExecutor;
import com.developmentontheedge.be5.metadata.util.ProcessController;
import com.developmentontheedge.dbms.ExtendedSqlException;

public abstract class DefaultSchemaReader implements DbmsSchemaReader
{
    @Override
    public String getDefaultSchema( BeSqlExecutor sql ) throws ExtendedSqlException
    {
        DatabaseConnector connector = sql.getConnector();
        try
        {
            Connection connection = connector.getConnection();
            try
            {
                return connection.getMetaData().getUserName();
            }
            finally
            {
                connector.releaseConnection( connection );
            }
        }
        catch(SQLException ex)
        {
            throw new ExtendedSqlException( connector.getConnectString(), "getMetaData().getUserName()", ex );
        }
    }

    @Override
    public Map<String, String> readTableNames( BeSqlExecutor sql, String defSchema, ProcessController controller ) throws SQLException
    {
        DatabaseConnector connector = sql.getConnector();
        Connection connection = connector.getConnection();
        ResultSet rs = null;
        Map<String, String> result = new HashMap<>();
        try
        {
            rs = connection.getMetaData().getTables( null, defSchema, null, new String[] {"TABLE", "VIEW"} );
            while(rs.next())
            {
                String name = rs.getString( 3 /*"TABLE_NAME"*/ ).toLowerCase();
                String type = rs.getString( 4 /*"TABLE_TYPE"*/ );
                result.put( name, type );
            }
        }
        finally
        {
            connector.close( rs );
            connector.releaseConnection( connection );
        }
        return result;
    }
}
