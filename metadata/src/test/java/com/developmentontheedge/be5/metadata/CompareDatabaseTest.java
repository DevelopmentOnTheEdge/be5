package com.developmentontheedge.be5.metadata;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.beanexplorer.enterprise.DatabaseConnector;
import com.beanexplorer.enterprise.client.testframework.TestDB;
import com.developmentontheedge.be5.metadata.exception.ProcessInterruptedException;
import com.developmentontheedge.be5.metadata.sql.BeSqlExecutor;
import com.developmentontheedge.be5.metadata.sql.DatabaseUtils;
import com.developmentontheedge.be5.metadata.sql.pojo.IndexInfo;
import com.developmentontheedge.be5.metadata.sql.pojo.SqlColumnInfo;
import com.developmentontheedge.be5.metadata.sql.schema.DbmsSchemaReader;
import com.developmentontheedge.be5.metadata.util.NullLogger;
import com.developmentontheedge.be5.metadata.util.ProcessController;
import com.developmentontheedge.dbms.ExtendedSqlException;

/**
 * Compares physical structure of 2 databases.
 *
 * BioStore project is used as example.
 * biostore - database created using standard ant setup.db
 * biostore_be4 - database created using new approach
 */
public class CompareDatabaseTest extends TestCase
{
    /* condo */
    /*private static String CONNECT_STRING = "jdbc:postgresql://localhost:5432/condo?user=condo;password=condo";
    private static String CONNECT_STRING_BE4 = "jdbc:postgresql://localhost:5432/condo_be4?user=condo;password=condo";*/
    /* ecity */
    /*private static String CONNECT_STRING = "jdbc:postgresql://localhost:5432/ecity?user=ecity;password=ecity";
    private static String CONNECT_STRING_BE4 = "jdbc:postgresql://localhost:5432/ecity_be4?user=ecity;password=ecity";*/
    /* veterans-v101*/
    /*private static String CONNECT_STRING = "jdbc:db2://localhost:50000/v101?user=db2admin;password=sa";
    private static String CONNECT_STRING_BE4 = "jdbc:db2://localhost:50000/v101_be4?user=db2admin;password=sa";*/
    /* biostore */
    private static String CONNECT_STRING = "jdbc:mysql://localhost:3306/biostore?user=biostore;password=biostore";
    private static String CONNECT_STRING_BE4 = "jdbc:mysql://localhost:3306/biostore_be4?user=biostore;password=biostore";
    /* tisnso */
    /*private static String CONNECT_STRING = "jdbc:oracle:thin:@newdev:1521:orcl?user=tisnso_tagir;password=tisnso;defaultRowPrefetch=1000";
    private static String CONNECT_STRING_BE4 = "jdbc:oracle:thin:@newdev:1521:orcl?user=tisnso_tagir2;password=tisnso;defaultRowPrefetch=1000";*/
    /* r03 */
    /*private static String CONNECT_STRING = "jdbc:sqlserver://winserv2012.dote.ru:1433;databaseName=r03_tagir;user=sa;password=sa2008";
    private static String CONNECT_STRING_BE4 = "jdbc:sqlserver://winserv2012.dote.ru:1433;databaseName=r03_tagir2;user=sa;password=sa2008";*/
    /*private static String CONNECT_STRING = "jdbc:sqlserver://winserv2012.dote.ru:1433;databaseName=lan3;user=sa;password=sa2008";
    private static String CONNECT_STRING_BE4 = "jdbc:sqlserver://winserv2012.dote.ru:1433;databaseName=lan4;user=sa;password=sa2008";*/
    
    private static final List<String> checkTables = Arrays.asList(
            //"attributes",
            "categories",
            "classifications",
            "daemons",
            "entities",
            "generic_ref_entities",
            "icons", 
            //"javascriptforms",
            //"javascripthandlers",
            "localizedmessages", 
            "operations", 
            "operations_per_query", 
            "operations_per_role", 
            "operationextension", 
            "pagecustomisation",
            "roles",
            "queries", 
            "queries_per_role",
            "queries_per_user", 
            "querysettings", 
            "quickfilteroptions",
            "staticpages",
            "systemSettings",
            "table_refs");

    private BeSqlExecutor sql;
    private BeSqlExecutor sqlBE4;
    
    protected CompareDatabaseTest(String name)
    {
        super(name);
    }
    
    protected CompareDatabaseTest()
    {
        super();
    }

    /**
     * Compares list of tables, if there is difference, print it in System.out 
     */
    public void testCompareTables() throws Exception
    {
        List<String> tables = sql.readStringList("sql.table.list");
        List<String> tablesBE4 = sqlBE4.readStringList("sql.table.list");

        List<String> errors = new ArrayList<>();
        Set<String> absentTables = new TreeSet<>(tables);
        absentTables.removeAll( tablesBE4 );
        for(String table : absentTables)
        {
            errors.add("Table "+table+" doesn't exist in BE4");
        }
        Set<String> absentTablesBE4 = new TreeSet<>(tablesBE4);
        absentTablesBE4.removeAll(tables);
        for(String table : absentTablesBE4)
        {
            errors.add("Table "+table+" doesn't exist in BE");
        }
        Set<String> bothTables = new TreeSet<>(tables);
        bothTables.retainAll( tablesBE4 );
        int missingEntries = 0;
        int extraEntries = 0;
        for(String table : bothTables)
        {
            int size1;
            int size2;
            try
            {
                size1 = sql.count( table );
                size2 = sqlBE4.count( table );
            }
            catch ( Exception e )
            {
                continue;
            }
            if(size1 != size2)
            {
                errors.add( "Table "+table+": number of rows differ ( BE: "+size1+"; BE4: "+size2+")" );
                if(size1 > size2)
                    missingEntries+=size1 - size2;
                else
                    extraEntries+=size2 - size1;
            }
        }
        if(!absentTables.isEmpty())
            errors.add( "Total missing tables in BE4: "+absentTables.size() );
        if(!absentTablesBE4.isEmpty())
            errors.add( "Total extra tables in BE4: "+absentTablesBE4.size() );
        if(missingEntries > 0)
            errors.add( "Total missing entries in BE4: "+missingEntries );
        if(extraEntries > 0)
            errors.add( "Total extra entries in BE4: "+extraEntries );
        
        validate( "Row counts", errors );
    }
    
    public void testCompareSchema() throws Exception
    {
        List<String> errors = new ArrayList<>();
        List<Map<String, List<String>>> schema = readSchema( sql );
        List<Map<String, List<String>>> schemaBE4 = readSchema( sqlBE4 );
        for(int i=0; i<schema.size(); i++)
        {
            errors.addAll( compareMaps( schema.get( i ), schemaBE4.get( i ) ) );
        }
        validate("Schema", errors);
    }

    private List<Map<String, List<String>>> readSchema( BeSqlExecutor sql ) throws SQLException, ProcessInterruptedException, ExtendedSqlException
    {
        ProcessController pc = new NullLogger();
        DbmsSchemaReader schemaReader = DatabaseUtils.getRdbms( sql.getConnector() ).getSchemaReader();
        String schema = schemaReader.getDefaultSchema( sql );
        List<Map<String, List<String>>> maps = new ArrayList<>();
        maps.add( convertTableMap( schemaReader.readTableNames( sql, schema, pc ) ) );
        maps.add( convertColumnMap( schemaReader.readColumns( sql, schema, pc ) ) );
        maps.add( convertIndexMap( schemaReader.readIndices( sql, schema, pc ) ) );
        return maps;
    }

    private Map<String, List<String>> convertTableMap( Map<String, String> tableNames )
    {
        Map<String, List<String>> tableMap = new TreeMap<>();
        for(Entry<String, String> entry: tableNames.entrySet())
            tableMap.put( "Table "+entry.getKey(), Collections.singletonList( entry.getValue() ) );
        return tableMap;
    }

    private Map<String, List<String>> convertColumnMap( Map<String, List<SqlColumnInfo>> columns )
    {
        Map<String, List<String>> columnMap = new TreeMap<>();
        for(Entry<String, List<SqlColumnInfo>> entry: columns.entrySet())
        {
            for(SqlColumnInfo column : entry.getValue())
            {
                columnMap.put(
                        "Column "+entry.getKey() + "." + column.getName(),
                        Arrays.asList( column.getType(), column.getDefaultValue(),
                                column.getEnumValues() == null ? null : Arrays.asList( column.getEnumValues() ).toString(),
                                String.valueOf( column.getEnumValues() == null ? column.getSize() : 0 ), String.valueOf( column.getPrecision() ) ) );
            }
        }
        return columnMap;
    }

    private Map<String, List<String>> convertIndexMap( Map<String, List<IndexInfo>> indices )
    {
        Map<String, List<String>> indexMap = new TreeMap<>();
        for(Entry<String, List<IndexInfo>> entry: indices.entrySet())
        {
            for(IndexInfo index : entry.getValue())
            {
                indexMap.put( "Index " + entry.getKey() + "." + index.getColumns(),
                        Collections.singletonList( String.valueOf( index.isUnique() ) ) );
            }
        }
        return indexMap;
    }

    @Override
    protected void setUp() throws IOException
    {
        DatabaseConnector connector = TestDB.getConnector(CONNECT_STRING);
        DatabaseConnector connectorBE4 = TestDB.getConnector(CONNECT_STRING_BE4);
        sql = new BeSqlExecutor( connector );
        sqlBE4 = new BeSqlExecutor( connectorBE4 );
    }

    private void validate( String title , List<String> errors )
    {
        if(!errors.isEmpty())
        {
            fail( title+"\n"+title.replaceAll( ".", "-" )+"\n"+String.join("\n", errors ) );
        }
    }
    
    static Map<String, List<String>> getQueryMap(BeSqlExecutor sql, String query) throws SQLException, ExtendedSqlException
    {
        Map<String, List<String>> result = new TreeMap<>();
        ResultSet rs = sql.executeNamedQuery( query );
        try
        {
            int columnCount = rs.getMetaData().getColumnCount();
            while(rs.next())
            {
                List<String> row = new ArrayList<>();
                for(int i=2; i<=columnCount; i++)
                {
                    row.add( sanitizeValue( rs.getString( i ) ) );
                }
                result.put( sanitizeValue( rs.getString( 1 ) ), row );
            }
        }
        finally
        {
            sql.getConnector().close( rs );
        }
        return result;
    }

    static String sanitizeValue( String rawValue )
    {
        String value = String.valueOf( rawValue ).replaceAll( "\\s+", " " ).replaceAll( "\\s*([,\\(\\)\\|]|\\*/|/\\*)\\s*", "$1" )
                .replaceAll(",SomeImpossibleRoleName", "").replaceAll("SomeImpossibleRoleName,?", "");
        if(!value.trim().isEmpty())
            value = value.trim();
        return value;
    }
    
    private List<String> compareQuery(String query) throws SQLException, ExtendedSqlException
    {
        Map<String, List<String>> mapBE = getQueryMap( sql, query );
        Map<String, List<String>> mapBE4 = getQueryMap( sqlBE4, query );
        return compareMaps( mapBE, mapBE4 );
    }

    private List<String> compareMaps( Map<String, List<String>> mapBE, Map<String, List<String>> mapBE4 )
    {
        List<String> errors = new ArrayList<>();
        Set<String> absentBE4 = new TreeSet<>(mapBE.keySet());
        absentBE4.removeAll( mapBE4.keySet() );
        Set<String> absentBE = new TreeSet<>(mapBE4.keySet());
        absentBE.removeAll( mapBE.keySet() );
        for(String key : absentBE4)
        {
            errors.add( "Absent in BE4: "+key+": "+mapBE.get(key) );
        }
        for(String key : absentBE)
        {
            errors.add( "Extra in BE4: "+key+": "+mapBE4.get(key) );
        }
        Set<String> both = new TreeSet<>(mapBE.keySet());
        both.retainAll( mapBE4.keySet() );
        int mismatch = 0;
        for(String key : both)
        {
            List<String> valBE = mapBE.get( key );
            List<String> valBE4 = mapBE4.get( key );
            if(!valBE.equals( valBE4 ))
            {
                errors.add( "Mismatch: "+key+":\n  BE: "+valBE+";\n BE4: "+valBE4 );
                mismatch++;
            }
        }
        if(!absentBE4.isEmpty())
            errors.add( "Total missing in BE4: "+absentBE4.size() );
        if(!absentBE.isEmpty())
            errors.add( "Total extra in BE4: "+absentBE.size() );
        if(mismatch > 0)
            errors.add( "Total mismatches: "+mismatch );
        return errors;
    }
    
    private void checkTable(String tableName) throws SQLException, ExtendedSqlException
    {
        validate( tableName, compareQuery( "test."+tableName ) );
    }
    
    public static Test suite()
    {
        TestSuite suite = new TestSuite(CompareDatabaseTest.class.getName());
        for(String table : checkTables)
        {
            suite.addTest( (new CompareDatabaseTest()).new TableTestCase( table ) );
        }
        suite.addTest( new CompareDatabaseTest("testSummary") );
        suite.addTest( new CompareDatabaseTest("testCompareTables") );
        suite.addTest( new CompareDatabaseTest("testCompareSchema") );
        return suite;
    }
    
    public class TableTestCase extends TestCase
    {
        private final String table;

        public TableTestCase(String table)
        {
            super(table);
            this.table = table;
        }
        
        @Override
        protected void setUp() throws Exception
        {
            CompareDatabaseTest.this.setUp();
        }

        @Override
        protected void runTest() throws Throwable
        {
            checkTable( table );
        }
    }
    
    public void testSummary() throws SQLException, ExtendedSqlException
    {
        List<String> errors = new ArrayList<>();
        int total = 0;
        for(String entry: checkTables)
        {
            Map<String, List<String>> mapBE = getQueryMap( sql, "test."+entry );
            Map<String, List<String>> mapBE4 = getQueryMap( sqlBE4, "test."+entry );
            Set<String> absentBE4 = new TreeSet<>(mapBE.keySet());
            absentBE4.removeAll( mapBE4.keySet() );
            Set<String> absentBE = new TreeSet<>(mapBE4.keySet());
            absentBE.removeAll( mapBE.keySet() );
            int extra = absentBE.size();
            int missing = absentBE4.size();
            Set<String> both = new TreeSet<>(mapBE.keySet());
            both.retainAll( mapBE4.keySet() );
            int mismatch = 0;
            for(String key : both)
            {
                List<String> valBE = mapBE.get( key );
                List<String> valBE4 = mapBE4.get( key );
                if(!valBE.equals( valBE4 ))
                {
                    mismatch++;
                }
            }
            if(extra > 0 || missing > 0 || mismatch > 0)
            {
                errors.add( entry+": missing "+missing+"; extra: "+extra+"; mismatch: "+mismatch );
                total += extra + missing + mismatch;
            }
        }
        if(total > 0)
            errors.add( "Total errors: "+total );
        validate("Summary", errors);
    }
}
