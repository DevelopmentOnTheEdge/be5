package com.developmentontheedge.be5.metadata.sql;

import java.util.Map;
import java.util.Map.Entry;

import com.developmentontheedge.be5.metadata.sql.macro.BeSQLMacroProcessorStrategy;
import com.developmentontheedge.be5.metadata.sql.macro.Db2MacroProcessorStrategy;
import com.developmentontheedge.be5.metadata.sql.macro.IMacroProcessorStrategy;
import com.developmentontheedge.be5.metadata.sql.macro.M4MacroProcessorStrategy;
import com.developmentontheedge.be5.metadata.sql.macro.MySqlMacroProcessorStrategy;
import com.developmentontheedge.be5.metadata.sql.macro.OracleMacroProcessorStrategy;
import com.developmentontheedge.be5.metadata.sql.macro.PostgresMacroProcessorStrategy;
import com.developmentontheedge.be5.metadata.sql.macro.SqlServerMacroProcessorStrategy;
import com.developmentontheedge.be5.metadata.sql.schema.Db2SchemaReader;
import com.developmentontheedge.be5.metadata.sql.schema.DbmsSchemaReader;
import com.developmentontheedge.be5.metadata.sql.schema.MySqlSchemaReader;
import com.developmentontheedge.be5.metadata.sql.schema.OracleSchemaReader;
import com.developmentontheedge.be5.metadata.sql.schema.PostgresSchemaReader;
import com.developmentontheedge.be5.metadata.sql.schema.SqlServerSchemaReader;
import com.developmentontheedge.be5.metadata.sql.type.Db2TypeManager;
import com.developmentontheedge.be5.metadata.sql.type.DbmsTypeManager;
import com.developmentontheedge.be5.metadata.sql.type.MySqlTypeManager;
import com.developmentontheedge.be5.metadata.sql.type.OracleTypeManager;
import com.developmentontheedge.be5.metadata.sql.type.PostgresTypeManager;
import com.developmentontheedge.be5.metadata.sql.type.SqlServerTypeManager;
import com.developmentontheedge.dbms.DbmsType;

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
    
    BESQL(new DbmsType("besql", 0 ), new BeSQLMacroProcessorStrategy(), new PostgresTypeManager(), null, "", "", "" );
    
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
