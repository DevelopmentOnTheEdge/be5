package com.developmentontheedge.be5.metadata.sql.schema;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.developmentontheedge.dbms.DbmsConnector;
import com.developmentontheedge.dbms.SqlExecutor;

import com.beanexplorer.enterprise.OperationSupport;
import com.developmentontheedge.be5.metadata.exception.ProcessInterruptedException;
import com.developmentontheedge.be5.metadata.model.ColumnFunction;
import com.developmentontheedge.be5.metadata.sql.pojo.IndexInfo;
import com.developmentontheedge.be5.metadata.sql.pojo.SqlColumnInfo;
import com.developmentontheedge.be5.metadata.util.ProcessController;
import com.developmentontheedge.dbms.ExtendedSqlException;

public class Db2SchemaReader extends DefaultSchemaReader
{
    private static final Pattern GENERIC_COLUMN_PATTERN = Pattern.compile( "^AS \\(\\s*\'(\\w+)\\.\' \\|\\| RTRIM\\( CAST\\( (\\w+) AS CHAR\\( \\d+ \\) \\) \\) \\)$" );

    @Override
    public String getDefaultSchema( SqlExecutor sql ) throws ExtendedSqlException
    {
        return super.getDefaultSchema( sql ).toUpperCase();
    }

    @Override
    public Map<String, List<SqlColumnInfo>> readColumns( SqlExecutor sql, String defSchema, ProcessController controller ) throws SQLException, ProcessInterruptedException
    {
        DatabaseConnector connector = sql.getConnector();
        Map<String, List<SqlColumnInfo>> result = new HashMap<>();
        ResultSet rs = connector.executeQuery( "SELECT tabname,colname,typename,nulls,default,length,scale,identity,text FROM syscat.columns c "+
        (defSchema == null?"":"WHERE c.tabschema='"+defSchema+"' ")+" ORDER BY c.tabname,c.colno");
        try
        {
            while(rs.next())
            {
                String tableName = rs.getString( 1 /*"TABNAME"*/ ).toLowerCase();
                List<SqlColumnInfo> list = result.get( tableName );
                if(list == null)
                {
                    list = new ArrayList<>();
                    result.put( tableName, list );
                }
                SqlColumnInfo info = new SqlColumnInfo();
                list.add(info);
                info.setName( rs.getString( 2 /*"COLNAME"*/ ) );
                info.setType( rs.getString( 3 /*"TYPENAME"*/ ) );
                info.setCanBeNull( rs.getString( 4 /*"NULLS"*/ ).equals( "Y" ) );
                info.setDefaultValue( rs.getString( 5 /*"DEFAULT"*/ ) );
                info.setSize( rs.getInt(6 /*"LENGTH"*/) );
                info.setPrecision( rs.getInt( 7 /* "SCALE" */) );
                info.setAutoIncrement( rs.getString(8 /*"IDENTITY"*/).equals( "Y" ) );
                String text = rs.getString( 9 /*"TEXT"*/ );
                if(text != null)
                {
                    Matcher m = GENERIC_COLUMN_PATTERN.matcher( text );
                    if(m.matches())
                    {
                        String colName = m.group( 2 );
                        info.setDefaultValue( new ColumnFunction( colName, ColumnFunction.TRANSFORM_GENERIC ).toString() );
                    }
                }
            }
        }
        finally
        {
            connector.close( rs );
        }
        for(Entry<String, List<SqlColumnInfo>> table : result.entrySet())
        {
            HashMap<String, String[]> enums = OperationSupport.loadEntityEnums( connector, table.getKey(), null );
            for(SqlColumnInfo column : table.getValue())
            {
                column.setEnumValues( enums.get( column.getName() ) );
            }
        }
        return result;
    }

    @Override
    public Map<String, List<IndexInfo>> readIndices( SqlExecutor sql, String defSchema, ProcessController controller ) throws SQLException
    {
        DbmsConnector connector = sql.getConnector();
        Map<String, List<IndexInfo>> result = new HashMap<>();
        ResultSet rs = connector.executeQuery( "SELECT i.tabname,i.indname,ic.colname,i.uniquerule "
            + "FROM syscat.indexes i "
            + "JOIN syscat.indexcoluse ic ON (i.indschema=ic.indschema AND i.indname=ic.indname) "+
        (defSchema == null?"":"WHERE i.tabschema='"+defSchema+"' ")+" ORDER BY i.tabname,i.indname,ic.colseq");
        try
        {
            IndexInfo curIndex = null;
            String lastTable = null;
            while(rs.next())
            {
                String tableName = rs.getString( 1 /*"TABNAME"*/ ).toLowerCase();
                String indexName = rs.getString( 2 /*"INDNAME"*/ );
                if(!tableName.equals( lastTable ) || curIndex == null || !curIndex.getName().equals( indexName ))
                {
                    List<IndexInfo> list = result.get( tableName );
                    if(list == null)
                    {
                        list = new ArrayList<>();
                        result.put( tableName, list );
                    }
                    curIndex = new IndexInfo();
                    lastTable = tableName;
                    list.add(curIndex);
                    curIndex.setName( indexName );
                    String unique = rs.getString( 4 /*"UNIQUERULE"*/ );
                    curIndex.setUnique( "U".equals( unique ) || "P".equals( unique ) );
                }
                String column = rs.getString( 3 /*"COLNAMES"*/);
                curIndex.addColumn( column );
            }
        }
        finally
        {
            connector.close( rs );
        }
        return result;
    }
}
