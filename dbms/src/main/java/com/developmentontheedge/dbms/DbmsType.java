package com.developmentontheedge.dbms;


/**
 * Type of Dbms along with some metainformation
 * 
 * @author lan
 */
public class DbmsType
{
    public static final DbmsType DB2 = new DbmsType( "db2", 50000 );
    public static final DbmsType MYSQL = new DbmsType( "mysql", 3306 )
    {
        @Override
        public String quoteString( String input )
        {
            return "\'"+input.replace( "\\", "\\\\" ).replace( "\'", "\'\'" )+"\'";
        }
    };
    public static final DbmsType ORACLE = new DbmsType( "oracle", 1521 );
    public static final DbmsType SQLSERVER = new DbmsType( "sqlserver", 1433 );
    public static final DbmsType POSTGRESQL = new DbmsType( "postgres", 5432 );

    private final String name;
    private final int defaultPort;

    public DbmsType( String name, int port )
    {
        this.name = name;
        this.defaultPort = port;
    }

    public String getName()
    {
        return name;
    }

    public int getDefaultPort()
    {
        return defaultPort;
    }

    @Override
    public String toString()
    {
        return name;
    }
    
    public String quoteString(String input)
    {
        return "\'"+input.replace( "\'", "\'\'" )+"\'";
    }
}
