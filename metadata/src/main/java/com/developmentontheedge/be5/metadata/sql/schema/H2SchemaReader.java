package com.developmentontheedge.be5.metadata.sql.schema;

import com.developmentontheedge.be5.metadata.exception.ProcessInterruptedException;
import com.developmentontheedge.be5.metadata.sql.pojo.IndexInfo;
import com.developmentontheedge.be5.metadata.sql.pojo.SqlColumnInfo;
import com.developmentontheedge.be5.metadata.util.ProcessController;
import com.developmentontheedge.dbms.DbmsConnector;
import com.developmentontheedge.dbms.SqlExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class H2SchemaReader extends DefaultSchemaReader
{
    private static final Pattern GENERIC_REF_INDEX_PATTERN =
            Pattern.compile("^\\(\\('(\\w+)\\.'::text \\|\\| \\(?\\(?\"?(\\w+)\"?\\)" +
                    "(::character varying\\)|)(::text\\)|)\\)$");
    private static final Pattern LOWER_INDEX_PATTERN = Pattern.compile("^lower\\(\\(\"?(\\w+)\"?\\)::text\\)$");
    private static final Pattern UPPER_INDEX_PATTERN = Pattern.compile("^upper\\(\\(\"?(\\w+)\"?\\)::text\\)$");
    private static final Pattern QUOTE_INDEX_PATTERN = Pattern.compile("^\"?(\\w+)\"?");

    private static final Pattern DEFAULT_DATE_PATTERN =
            Pattern.compile("^to_date\\('(\\d+\\-\\d+\\-\\d+)'::text, 'YYYY-MM-DD'::text\\)$");
    private static final String[] SUFFICES = {"::character varying", "::date", "::text", "::integer"};

    private static final Pattern ENUM_VALUES_PATTERN = Pattern.compile("\'(.+?)\'::(character varying|text)");

    @Override
    public Map<String, List<SqlColumnInfo>> readColumns(SqlExecutor sql, String defSchema, ProcessController controller)
            throws SQLException, ProcessInterruptedException
    {
        DbmsConnector connector = sql.getConnector();
        Map<String, List<SqlColumnInfo>> result = new HashMap<>();
        ResultSet rs = connector.executeQuery("SELECT " +
                "c.TABLE_NAME, " +
                "c.COLUMN_NAME, " +
                "c.COLUMN_DEFAULT, " +
                /*"c.TYPE_NAME, " +*/
                "c.DATA_TYPE, " +
                /*"c.data_type, " +*/
                "c.CHARACTER_MAXIMUM_LENGTH, " +
                "c.NUMERIC_PRECISION, " +
                "c.NUMERIC_SCALE, " +
                "c.IS_NULLABLE, " +
                //"c.CHECK_CONSTRAINT " +
                "NULL " +
                "FROM INFORMATION_SCHEMA.COLUMNS c " +
                "WHERE c.TABLE_SCHEMA <> 'INFORMATION_SCHEMA'");
        try
        {
            while (rs.next())
            {
                String tableName = rs.getString(1 /*"table_name"*/).toLowerCase();
                if (!tableName.equals(tableName.toLowerCase()))
                    continue;
                List<SqlColumnInfo> list = result.get(tableName);
                if (list == null)
                {
                    list = new ArrayList<>();
                    result.put(tableName, list);
                }
                SqlColumnInfo info = new SqlColumnInfo();
                list.add(info);
                info.setName(rs.getString(2 /*"column_name"*/));
                info.setType(rs.getString(4 /*"udt_name"*/));
                info.setCanBeNull("YES".equals(rs.getString(8 /*"is_nullable"*/)));
                String defaultValue = rs.getString(3 /*"column_default"*/);
                if (defaultValue != null)
                {
                    for (String suffix : SUFFICES)
                    {
                        if (defaultValue.endsWith(suffix))
                            defaultValue = defaultValue.substring(0, defaultValue.length() - suffix.length());
                    }
                    defaultValue = DEFAULT_DATE_PATTERN.matcher(defaultValue).replaceFirst("'$1'");
                }
                info.setDefaultValue(defaultValue);
                info.setSize(rs.getInt(5 /*"character_maximum_length"*/));
                if (rs.wasNull())
                {
                    info.setSize(rs.getInt(6 /*"numeric_precision"*/));
                }
                info.setPrecision(rs.getInt(7 /* "numeric_precision" */));
                info.setAutoIncrement(defaultValue != null && defaultValue.startsWith("nextval"));

                String check = rs.getString(9 /* "check_clause" */);
                if (check == null)
                    continue;
                Matcher matcher = ENUM_VALUES_PATTERN.matcher(check);
                List<String> vals = new ArrayList<>();
                while (matcher.find())
                {
                    vals.add(matcher.group(1));
                }
                info.setEnumValues(vals.toArray(new String[vals.size()]));
                controller.setProgress(0); // Just to check for interrupts
            }
        }
        finally
        {
            connector.close(rs);
        }
        return result;
    }

    @Override
    public String getDefaultSchema(SqlExecutor sqlExecutor)
    {
        DbmsConnector connector = null;
        ResultSet rs = null;
        try
        {
            connector = sqlExecutor.getConnector();
            rs = sqlExecutor.getConnector().executeQuery("SELECT SCHEMA()");
            rs.next();
            String search_path = rs.getString(1);
            //for different settings: default and after setup
            if (search_path.contains(","))
                return search_path.split(",")[1].trim();
            else
                return search_path.trim();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            if (connector != null) connector.close(rs);
        }
    }

    @Override
    public Map<String, String> readTableNames(SqlExecutor sql, String defSchema, ProcessController controller)
            throws SQLException
    {
        DbmsConnector connector = sql.getConnector();
        Map<String, String> result = new HashMap<>();
        ResultSet rs = connector.executeQuery("SELECT table_name,table_type FROM information_schema.tables t " +
                "WHERE table_schema='" + defSchema + "' AND table_type IN ('TABLE','VIEW')");
        try
        {
            while (rs.next())
            {
                String tableName = rs.getString(1 /*"table_name"*/).toLowerCase();
//                if(!tableName.equals( tableName.toLowerCase() ))
//                    continue;
                String type = rs.getString(2 /*"table_type"*/);
//                if ( "TABLE".equals( type ) )
//                {
//                    type = "TABLE";
//                }
                result.put(tableName, type);
            }
        }
        finally
        {
            connector.close(rs);
        }
        return result;
    }

    @Override
    public Map<String, List<IndexInfo>> readIndices(SqlExecutor sql, String defSchema, ProcessController controller)
            throws SQLException, ProcessInterruptedException
    {
//        DbmsConnector connector = sql.getConnector();
        Map<String, List<IndexInfo>> result = new HashMap<>();
//        ResultSet rs = connector.executeQuery( "SELECT ct.relname AS TABLE_NAME, i.indisunique AS IS_UNIQUE,
// ci.relname AS INDEX_NAME, "+
//            "pg_catalog.pg_get_indexdef(ci.oid, (i.keys).n, false) AS COLUMN_NAME, (i.keys).n AS ORDINAL "+
//            "FROM pg_catalog.pg_class ct "+
//            "JOIN pg_catalog.pg_namespace n "+
//            "ON (ct.relnamespace = n.oid)"+
//            "JOIN ("+
//                "SELECT i.indexrelid, i.indrelid, i.indisunique, "+
//                "information_schema._pg_expandarray(i.indkey) AS keys FROM pg_catalog.pg_index i "+
//                ") i "+
//            "ON (ct.oid = i.indrelid) "+
//            "JOIN pg_catalog.pg_class ci ON (ci.oid = i.indexrelid) "+
//        (defSchema == null?"":"AND n.nspname = '"+defSchema+"' ")+" ORDER BY 1,3,5");
//        try
//        {
//            IndexInfo curIndex = null;
//            String lastTable = null;
//            while(rs.next())
//            {
//                String tableName = rs.getString( 1 /*"TABLE_NAME"*/ );
//                String indexName = rs.getString( 3 /*"INDEX_NAME"*/ );
//                if(indexName == null)
//                    continue;
//                if(!tableName.equals( lastTable ) || curIndex == null || !curIndex.getName().equals( indexName ))
//                {
//                    List<IndexInfo> list = result.get( tableName );
//                    if(list == null)
//                    {
//                        list = new ArrayList<>();
//                        result.put( tableName, list );
//                    }
//                    curIndex = new IndexInfo();
//                    lastTable = tableName;
//                    list.add(curIndex);
//                    curIndex.setName( indexName );
//                    curIndex.setUnique( rs.getBoolean( 2 /*"IS_UNIQUE"*/ ) );
//                }
//                String column = rs.getString( 4 /*"COLUMN_NAME"*/ );
//                column = GENERIC_REF_INDEX_PATTERN.matcher( column ).replaceFirst( "generic($2)" );
//                column = UPPER_INDEX_PATTERN.matcher( column ).replaceFirst( "upper($1)" );
//                column = LOWER_INDEX_PATTERN.matcher( column ).replaceFirst( "lower($1)" );
//                column = QUOTE_INDEX_PATTERN.matcher( column ).replaceFirst( "$1" );
//                curIndex.addColumn( column );
//                controller.setProgress( 0 );
//            }
//        }
//        finally
//        {
//            connector.close( rs );
//        }
        return result;
    }
}
