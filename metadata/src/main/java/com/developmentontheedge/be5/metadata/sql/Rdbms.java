package com.developmentontheedge.be5.metadata.sql;

import com.developmentontheedge.be5.metadata.sql.macro.*;
import com.developmentontheedge.be5.metadata.sql.schema.*;
import com.developmentontheedge.be5.metadata.sql.type.*;
import com.developmentontheedge.dbms.DbmsConnector;
import com.developmentontheedge.dbms.DbmsType;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum Rdbms
{
    DB2( DbmsType.DB2, 
            new Db2MacroProcessorStrategy(), 
            new Db2TypeManager(), 
            new Db2SchemaReader(), 
            "org.eclipse.datatools.enablement.ibm.db2.luw.connectionProfile", 
            "DriverDefn.org.eclipse.datatools.enablement.ibm.db2.luw.jdbc4.driverTemplate.IBM Data Server Driver for JDBC and SQLJ (JDBC 4.0) Default", "v97" ),
    MYSQL( DbmsType.MYSQL, 
            new MySqlMacroProcessorStrategy(), 
            new MySqlTypeManager(), 
            new MySqlSchemaReader(), 
            "org.eclipse.datatools.enablement.mysql.connectionProfile", 
            "DriverDefn.org.eclipse.datatools.enablement.mysql.5_1.driverTemplate.MySQL JDBC Driver", "5" ),
    ORACLE( DbmsType.ORACLE, 
            new OracleMacroProcessorStrategy(), 
            new OracleTypeManager(), 
            new OracleSchemaReader(), 
            "org.eclipse.datatools.enablement.oracle.connectionProfile", 
            "DriverDefn.org.eclipse.datatools.enablement.oracle.11.driverTemplate.Oracle Thin Driver", "10" ),
    SQLSERVER( DbmsType.SQLSERVER, 
            new SqlServerMacroProcessorStrategy(),
            new SqlServerTypeManager(), 
            new SqlServerSchemaReader(), 
            "org.eclipse.datatools.enablement.msft.sqlserver.connectionProfile",
            "DriverDefn.org.eclipse.datatools.enablement.msft.sqlserver.2008.driverTemplate.Microsoft SQL Server 2008 JDBC Driver", "2008"),
    POSTGRESQL( DbmsType.POSTGRESQL, 
            new PostgresMacroProcessorStrategy(), 
            new PostgresTypeManager(), 
            new PostgresSchemaReader(), 
            "org.eclipse.datatools.enablement.postgresql.connectionProfile", 
            "DriverDefn.org.eclipse.datatools.enablement.postgresql.postgresqlDriverTemplate.PostgreSQL JDBC Driver", "91" ),
    BESQL(DbmsType.BESQL,
            new BeSQLMacroProcessorStrategy(),
            new PostgresTypeManager(),
            null, "", "", "" ),
    H2(DbmsType.H2,
            new PostgresMacroProcessorStrategy(),
            new PostgresTypeManager(),
            new PostgresSchemaReader(),
            "", "org.h2.Driver", "" );

    private static final Logger log = Logger.getLogger(Rdbms.class.getName());

    public static Rdbms getRdbms(final String url)
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
        if(realUrl.startsWith( "h2:" ))
        {
            return Rdbms.H2;
        }
        if(realUrl.startsWith( "sqlserver:" ) || realUrl.startsWith( "microsoft:sqlserver:" ) || realUrl.startsWith( "jtds:sqlserver:" ))
        {
            return Rdbms.SQLSERVER;
        }

        log.log(Level.SEVERE, "Database type not supported or not determined: " + realUrl);
        throw new RuntimeException("Database type not supported or not determined: " + realUrl);
    }

    public static Rdbms getRdbms(DbmsConnector connector)
    {
        switch( connector.getType() )
        {
            case MYSQL:         return Rdbms.MYSQL; 
            case DB2:           return Rdbms.DB2; 
            case ORACLE:        return Rdbms.ORACLE; 
            case POSTGRESQL:    return Rdbms.POSTGRESQL; 
            case SQLSERVER:     return Rdbms.SQLSERVER; 
            case BESQL:         return Rdbms.BESQL;
            case H2:            return Rdbms.H2;
        }

        throw new IllegalStateException("Unsupported connector: " + connector.getConnectString());
    }
    
  
    ///////////////////////////////////////////////////////////////////
    // RDBMS implementation
    //
    
    private final DbmsType type;
    private final IMacroProcessorStrategy macroProcessor;
    private final DbmsTypeManager typeManager;
    private final DbmsSchemaReader schemaReader;
    private final String providerId;
    private final String driverDefinition;
    private final String version;
    
    private Rdbms( DbmsType type, IMacroProcessorStrategy macroProcessor, DbmsTypeManager typeManager, DbmsSchemaReader schemaReader, String providerId, String driverDefinition, String version )
    {
        this.type = type;
        this.macroProcessor = macroProcessor;
        this.typeManager = typeManager;
        this.schemaReader = schemaReader;
        this.providerId = providerId;
        this.driverDefinition = driverDefinition;
        this.version = version;
    }
    
    public String getName()
    {
        return type.getName();
    }
    
    public String getAntName()
    {
        return type.getName().equals( "postgres" ) ? "postgresql" : type.getName();
    }

    public IMacroProcessorStrategy getMacroProcessorStrategy()
    {
        return macroProcessor;
    }

    public DbmsTypeManager getTypeManager()
    {
        return typeManager;
    }

    public DbmsSchemaReader getSchemaReader()
    {
        return schemaReader;
    }

    public String getProviderId()
    {
        return providerId;
    }
    
    public String getDriverDefinition()
    {
        return driverDefinition;
    }
    
    public DbmsType getType()
    {
        return type;
    }

    public int getDefaultPort()
    {
        return type.getDefaultPort();
    }

    public String createConnectionUrl( String host, int port, String database, Map<String, String> properties )
    {
        switch(this)
        {
        case ORACLE:
            return "jdbc:oracle:thin:@"+host+":"+port+":"+(database == null ? properties.get( "SID" ) : database);
        case H2:
            return "jdbc:h2:" + host;
        case SQLSERVER:
            if("jtds".equals( properties.get( "driver" )))
            {
                StringBuilder url = new StringBuilder( "jdbc:jtds:sqlserver://" ).append( host ).append( ':' ).append( port ).append( '/' )
                        .append( database );
                for(Entry<String, String> entry : properties.entrySet())
                {
                    if(!entry.getKey().equals( "driver" ))
                        url.append(';').append( entry.getKey() ).append( '=' ).append( entry.getValue() );
                }
                return url.toString();
            }
            return "jdbc:sqlserver://"+host+":"+port+";databaseName="+database;
        default:
            StringBuilder url = new StringBuilder( "jdbc:" ).append( toString().toLowerCase() ).append( "://" ).append( host ).append( ':' )
                    .append( port ).append( '/' ).append( database );
            if(!properties.isEmpty())
            {
                if(this == DB2)
                    url.append( ':' );
                else
                    url.append( '?' );
                for(Entry<String, String> entry : properties.entrySet())
                {
                    url.append( entry.getKey() ).append( '=' ).append( entry.getValue() ).append( ';' );
                }
            }
            return url.toString();
        }
    }

    public static final int DB2_INDEX_LENGTH = 18;

    public String getDefaultVersion()
    {
        return version;
    }
}
