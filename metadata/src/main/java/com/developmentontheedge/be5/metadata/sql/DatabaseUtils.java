package com.developmentontheedge.be5.metadata.sql;

import com.beanexplorer.enterprise.DatabaseConnector;
import com.beanexplorer.enterprise.client.testframework.dbms.DBMSBase;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.dbms.ExtendedSqlException;
import com.developmentontheedge.dbms.SqlExecutor;

/**
 * @author Andrey Anisimov <andrey@developmentontheedge.com>
 */
public class DatabaseUtils
{
    public static Rdbms getRdbms( final String url )
    {
        String realUrl = url.startsWith( "jdbc:" )?url.substring( "jdbc:".length() ):url; 
        if(realUrl.startsWith( "mysql:" ))
        {
            return Rdbms.MYSQL;
        }
        if(realUrl.startsWith( "db2:" ))
        {
            return Rdbms.DB2;
        }
        if(realUrl.startsWith( "oracle:" ))
        {
            return Rdbms.ORACLE;
        }
        if(realUrl.startsWith( "postgresql:" ))
        {
            return Rdbms.POSTGRESQL;
        }
        if(realUrl.startsWith( "sqlserver:" ) || realUrl.startsWith( "microsoft:sqlserver:" ) || realUrl.startsWith( "jtds:sqlserver:" ))
        {
            return Rdbms.SQLSERVER;
        }
        return null;
    }
    
    public static Rdbms getRdbms( final DatabaseConnector connector )
    {
        if ( connector.isDb2() )
        {
            return Rdbms.DB2;
        }
        else if ( connector.isMySQL() )
        {
            return Rdbms.MYSQL;
        }
        else if ( connector.isOracle() )
        {
            return Rdbms.ORACLE;
        }
        else if ( connector.isSQLServer() )
        {
            return Rdbms.SQLSERVER;
        }
        else if ( connector.isPostgreSQL() )
        {
            return Rdbms.POSTGRESQL;
        }
        throw new IllegalStateException( "Unsupported connector: "+connector.getConnectString() );
    }
    
    public static String formatUrl( final String baseURL, final String user, final String password )
    {
        final String correctedPassword = password == null ? "" : password;
        final String additionalOptions = baseURL.startsWith( "jdbc:oracle" ) ? ";defaultRowPrefetch=1000" : "";
        
        if ( baseURL.endsWith( ";" ))
        {
            return String.format( "%s;user=%s;password=%s%s", baseURL, user, correctedPassword, additionalOptions );
        }
        else if ( baseURL.contains( "?" ) )
        {
            return String.format( "%s;user=%s;password=%s%s", baseURL, user, correctedPassword, additionalOptions );
        }
        else
        {
            return String.format( "%s?user=%s;password=%s%s", baseURL, user, correctedPassword, additionalOptions );
        }
    }
    
    public static void setSystemSetting(final SqlExecutor sql, final String category, final String name, final String value) throws ExtendedSqlException
    {
        sql.exec( "sql.delete.system.setting", category, name );
        sql.exec( "sql.insert.system.setting", category, name, value );
    }
    
    public static void clearMetadataCache(final SqlExecutor sql)
    {
        try
        {
            sql.startSection( "Clear all caches" );
            setSystemSetting( sql, "system", "CACHES_TO_CLEAR", "metadata_*,localizedMessagesCache" );
        }
        catch ( ExtendedSqlException e )
        {
            // ignore
        }
    }

    public static void clearAllCache(final SqlExecutor sql)
    {
        try
        {
            sql.startSection( "Clear all caches" );
            setSystemSetting( sql, "system", "CACHES_TO_CLEAR", "all" );
        }
        catch ( ExtendedSqlException e )
        {
            // ignore
        }
    }

    public static DatabaseConnector createConnector( final Project project )
    {
        final BeConnectionProfile connectionProfile = project.getConnectionProfile();
        
        if ( connectionProfile == null )
            throw new RuntimeException("Connection profile is not found in project "+project.getName());
        
        return createConnector( connectionProfile );
    }

    private static DatabaseConnector createConnector( final BeConnectionProfile connectionProfile )
    {
        final String url = formatUrl( connectionProfile.getConnectionUrl(), connectionProfile.getUsername(), connectionProfile.getPassword() );
        final DatabaseConnector connector = DBMSBase.createConnector( url );
        
        return connector;
    }

    public static void setSystemSetting( final SqlExecutor sqlExecutor, final String name, final String value ) throws ExtendedSqlException
    {
        setSystemSetting( sqlExecutor, "system", name, value );
    }
}
