package com.developmentontheedge.be5.api.operationstest.analyzers;


import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.metadata.util.StringUtils;
import com.developmentontheedge.be5.model.UserInfo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public abstract class DatabaseAnalyzerSupport implements DatabaseAnalyzer
{
    private static final Logger log = Logger.getLogger(DatabaseAnalyzerSupport.class.getName());

    protected DatabaseService connector;

    protected Class AccessibleViewsListClass;
    protected Class AccessibleOperationsListClass;
    protected Class QueryInfoClass;
    protected Class CategoryNavigationListClass;
    protected int maxCharLiteralLength = -2;

    public DatabaseAnalyzerSupport( DatabaseService connector )
    {
        this.connector = connector;
    }

    @Override
    public String makeSoundex( String field )
    {
        //FIXME
        return field;
    }

    @Override
    public String makeCleanChars( String field, String pattern )
    {
        //FIXME
        return field;
    }

    @Override
    public String makeLevenshtein( String field1, String field2 )
    {
        return "LEVENSHTEIN(" + field1 + ", " + field2 + ")" ;
    }

    protected Map processRefs(String context, UserInfo ui, String sql ) throws SQLException
    {
        Map refs = new HashMap();
//        for( DynamicPropertySet dps : Utils.readAsRecords( connector, sql, ReferencesDPSCache.getInstance() ) )
//        {
//            String columnsFrom = ( String )dps.getValue( "columnsFrom" );
//            String tableTo = ( String )dps.getValue( "tableTo" );
//            String columnsTo = ( String )dps.getValue( "columnsTo" );
//            String query = CryptoUtils.simpleDecrypt( dps.getValueAsString( "___hashQuery" ), dps.getValueAsString( "query" ) );
//            query = Utils.putPlaceholders( connector, query, ui, context );
//            Object oID = dps.getValue( "ID" );
//            String ID = oID != null ? oID.toString() : null;
//
//            String qname = ( String )dps.getValue( "qname" );
//
//            refs.put( columnsFrom, new String[]{ tableTo, columnsTo, query, ID, qname } );
//        }
        return refs;
    }

    @Override
    public String getCaseCorrectedIdentifier( String identifier )
    {
        return getIdentifierCase().normalize(identifier);
    }

    @Override
    public String getMinus()
    {
        return "EXCEPT";
    }

    @Override
    public Map readReferences( String context, UserInfo ui, String entity, String columnNameList ) throws SQLException
    {
        // try to utilize available reference information from 'table_refs' table
        // this table contains emulation of referential integrity checks
        // use columnNameList from the previous step to select the relevant columns
           
        String colsFromExpr = "t.columnsFrom";

//        if( connector.isDb2() )
//        {
//            colsFromExpr = "UCASE(t.columnsFrom)";
//        }
//        else if( connector.getAnalyzer().getIdentifierCase() == IdentifierCase.UPPER )
//        {
//            colsFromExpr = "UPPER(t.columnsFrom)";
//        }
//        else if( connector.getAnalyzer().getIdentifierCase() == IdentifierCase.LOWER )
//        {
//            colsFromExpr = "LOWER(t.columnsFrom)";
//        }

        StringBuffer sql = new StringBuffer( "SELECT " );
//        sql.append( " " ).append( colsFromExpr ).append( " AS \"columnsFrom\"," );
//        sql.append( " t.tableTo AS \"tableTo\", t.columnsTo AS \"columnsTo\", q.query AS \"query\", q." + connector.getAnalyzer().quoteIdentifier( "___hashQuery" ) + " AS \"___hashQuery\", q.ID AS \"ID\", q.name AS \"qname\" " );
//        sql.append( " FROM table_refs t " );
//        sql.append( " LEFT JOIN queries q ON t.selectionViewID = q.ID " );
//        sql.append( "WHERE t.tableFrom = '" ).append( entity ).append( "' " );
//        sql.append( "     AND " ).append( colsFromExpr ).append( " IN " ).append( columnNameList ).append( " AND t.tableTo IS NOT NULL " );

        return processRefs( context, ui, sql.toString() );
    }

    @Override
    public String makeCategoryFilter( String query, String tableName, String primaryKey, String category, boolean isUncategorized, String classificationsTable )
    {
        if( query == null )
        {
            return null;
        }
        //if( query.indexOf( "\"" + DatabaseConstants.ID_COLUMN_LABEL + "\"" ) < 0 )
        //    return query;
        String alias = tableName;
        StringTokenizer tz = new StringTokenizer( query.trim() );
        if( tz.hasMoreTokens() && tz.nextToken( " \r\n\t\f" ).equalsIgnoreCase( "select" ) )
        {
            alias = tz.nextToken( " \r\n\t\f." );
            if( Arrays.asList( "ALL", "DISTINCT", "DISTINCTROW" ).contains( alias.toUpperCase() ) )
            {
                alias = tz.nextToken( " \r\n\t\f." );
            }
            else if( alias.equalsIgnoreCase( "TOP" ) )
            {
                tz.nextToken( " \r\n\t\f" );
                alias = tz.nextToken( " \r\n\t\f." );
            }
        }
        else
        {
            return query;
        }

        String concatExpr = null;
        boolean bNewApproach = false;
//        try
//        {
//            bNewApproach = Utils.columnExists( connector, classificationsTable, "entity" );
//        }
//        catch( SQLException exc )
//        {
//            Logger.error( cat, "When checking for entity column in classifications", exc );
//        }
//
//        String genRef = makeGenericRefExpr( tableName, alias + "." + primaryKey );
//        if( bNewApproach )
//        {
//            concatExpr = "'" + tableName + "' = " + classificationsTable + ".entity AND " + alias + "." + primaryKey + " = " + classificationsTable + ".recordID ";
//        }
//        else
//        {
//            concatExpr = genRef + " = " + classificationsTable + ".recordID ";
//        }
//
        String fExpr1 = isUncategorized ?
                        " LEFT JOIN " + classificationsTable + " ON " + concatExpr + " WHERE " + classificationsTable + ".ID IS NULL " :
                        " INNER JOIN " + classificationsTable + " ON " + classificationsTable + ".categoryID = " + category + " AND " + concatExpr + " ";
//        String fExpr2 = isUncategorized ? fExpr1 + "AND " : fExpr1 + "WHERE ";
//
//        if( connector.isOracle() )
//        {
//            fExpr1 = isUncategorized ?
//                            " WHERE NOT EXISTS ( SELECT 1 FROM " + classificationsTable + " ccc WHERE ccc.recordID = " + genRef + " ) " :
//                            " , " + classificationsTable + " WHERE " + classificationsTable + ".categoryID = '" + category + "' AND " + concatExpr + " ";
//            fExpr2 = fExpr1 + " AND ";
//        }
//
//        boolean bWasFrom = false;
//        while( tz.hasMoreTokens() )
//        {
//            String next = tz.nextToken( " \r\n\t\f" );
//            if( Utils.skipInternalSelect( next, tz ) )
//            {
//                continue;
//            }
//            if( !bWasFrom )
//            {
//                if( next.equalsIgnoreCase( "FROM" ) )
//                {
//                    bWasFrom = true;
//                }
//                continue;
//            }
//            if( next.equalsIgnoreCase( "ORDER" ) || next.equalsIgnoreCase( "GROUP" ) )
//            {
//                int index = query.lastIndexOf( next );
//                return query.substring( 0, index ) + fExpr1 + query.substring( index );
//            }
//
//            if( next.equalsIgnoreCase( "WHERE" ) )
//            {
//                int index = query.lastIndexOf( next );
//                return query.substring( 0, index ) + fExpr2 + query.substring( index + 5 );
//            }
//        }

        return query + fExpr1;
    }

//    @Override
//    public AccessibleViewsList getAccessibleViews( UserInfo ui, String context, String targetPlatform )
//    {
//        try
//        {
//            return ( AccessibleViewsList )AccessibleViewsListClass.getDeclaredConstructors()[ 0 ].
//                    newInstance( new Object[]{ connector, this, ui, context, targetPlatform } );
//        }
//        catch( Exception exc )
//        {
//            Logger.fatal( cat, "Unable to instantiate", exc );
//        }
//        return null;
//    }
//
//    @Override
//    public AccessibleOperationsList getAccessibleOperations( UserInfo ui, String context, String targetPlatform, String entity, String queryID )
//    {
//        try
//        {
//            return ( AccessibleOperationsList )AccessibleOperationsListClass.getDeclaredConstructors()[ 0 ].
//                    newInstance( new Object[]{ connector, this, ui, context, targetPlatform, entity, queryID } );
//        }
//        catch( Exception exc )
//        {
//            Logger.fatal( cat, "Unable to instantiate", exc );
//        }
//        return null;
//    }
//
//    @Override
//    public QueryInfo getQueryInfo( UserInfo ui, String context, String targetPlatform, String queryId, String tableId, String queryName, Map presetValues )
//    {
//        if( "0".equals( queryId ) )
//        {
//            queryId = null;
//        }
//
//        try
//        {
//            return ( QueryInfo )QueryInfoClass.getDeclaredConstructors()[ 0 ].
//                    newInstance( new Object[]{ connector, ui, context, targetPlatform, queryId, tableId, queryName, presetValues } );
//        }
//        catch( Exception exc )
//        {
//            Logger.fatal( cat, "Unable to instantiate", exc );
//        }
//        return null;
//    }
//
//    @Override
//    public CategoryNavigationList listCategoriesForNavigation( UserInfo ui, String category, String entity )
//    {
//        try
//        {
//            return ( CategoryNavigationList )CategoryNavigationListClass.getDeclaredConstructors()[ 0 ].
//                    newInstance( new Object[]{ connector, ui, category, entity } );
//        }
//        catch( Exception exc )
//        {
//            Logger.fatal( cat, "Unable to instantiate", exc );
//        }
//        return null;
//    }

    @Override
    public boolean dropTableIfExists( String table )
    {
        try
        {
            connector.executeUpdate( "DROP TABLE " + table );
        }
        catch( SQLException exc )
        {
            log.warning( "Unable to drop table \"" + table + "\", reason: " + exc.getMessage() );
            return false;
        }
        return true;
    }

    @Override
    public String quoteUnsafeIdentifier( String tok )
    {
        if( isSafeIdentifier( tok ) )
        {
            return tok;
        }
        return quoteIdentifier( tok );
    }

    private static String newUniqueName( String prefix, String oldName, int indexNumber )
    {
        String newName = prefix + System.currentTimeMillis() + "_" + indexNumber + oldName;

        if( newName.length() > 30 )
        {
            newName = newName.substring( 0, 30 );
        }

        return newName;
    }


    @Override
    public String getLastInsertID( Connection conn, String table, String field ) throws SQLException
    {
        return getLastInsertID( conn );
    }

    @Override
    public String getLastInsertID( Connection conn, String insertSQL ) throws SQLException
    {
        return getLastInsertID( conn );
    }

    @Override
    public String makeMonthsDiff( Object date1, Object date2 )
    {
        return "( " + date1 + " - " + date2 + " )";
    }

    /**
    * Returns set of Strings with SQL instructions that create
    * indexes for table newName as indexes for origName. Remove
    * duplicate indexes, i.e. indexes for exactly the same set
    * of columns.
    *
    */
    @Override
    public List<String> makeIndexesLikeExpr(String origName, String newName)
    {

        Map indexes = new HashMap(); // <indexname, <pos, row> >
        Map<String,Boolean> uniqueMap = new HashMap<String,Boolean>(); 

//        String importID = Utils.subst( newName, origName, "" );
//
        List<String> ret = new ArrayList<String>();
//
//        Connection conn = null;
//        ResultSet rs = null;
//
//        try
//        {
//            List<String> computedColumns = Utils.readComputedColumns( connector, origName );
//            conn = connector.getConnection();
//            origName = connector.getAnalyzer().getCaseCorrectedIdentifier( origName );
//
//            if( connector.isOracle() )
//            {
//                rs = conn.getMetaData().getIndexInfo( null, connector.getConnectionUserName(), origName, false, true );
//            }
//            else
//            {
//                rs = conn.getMetaData().getIndexInfo( null, null, origName, false, true );
//            }
//
//            while( rs.next() )
//            {
//                String indexName = rs.getString( "INDEX_NAME" );
//
//                if( indexName == null )
//                {
//                    continue;
//                }
//
//                if( connector.isOracle() && indexName.startsWith( "SYS_" ) )
//                {
//                    continue;
//                }
//
//                if( connector.isMySQL() && "PRIMARY".equals( indexName ) )
//                {
//                    continue;
//                }
//
//                Short pos = Short.valueOf( rs.getShort( "ORDINAL_POSITION" ) );
//                String columnName = rs.getString( "COLUMN_NAME" );
//
//                if ( connector.isOracle() && columnName.startsWith( "SYS_" ))
//                {
//                    // it's the functional index, we can't do anything here
//                    continue;
//                }
//
//                Boolean isUnique = !rs.getBoolean( "NON_UNIQUE" );
//
//                if( !"0".equals( importID ) )
//                {
//                    uniqueMap.put( indexName, isUnique );
//                }
//
//                Map rows = ( Map )indexes.get( indexName );
//
//                if( rows == null )
//                {
//                    rows = new HashMap();
//                    indexes.put( indexName, rows );
//                }
//
//                rows.put( pos, columnName );
//            }
//
//            Iterator indexIterator = indexes.keySet().iterator();
//
//            Set columnSets = new HashSet();
//
//            int indexNumber = 0;
//            while( indexIterator.hasNext() )
//            {
//                String sql = "";
//
//                String indexName = ( String )indexIterator.next();
//                Map colMap = ( Map )indexes.get( indexName );
//
//                HashSet colSet = new HashSet( colMap.values() );
//                if( columnSets.contains( colSet ) )
//                {
//                    continue;
//                }
//                else
//                {
//                    columnSets.add( colSet );
//                }
//
//                sql += "CREATE ";
//
//                if( Boolean.TRUE.equals( uniqueMap.get( indexName ) ) )
//                {
//                    sql += "UNIQUE";
//                }
//
//                sql += " INDEX " + newUniqueName( "IDX_", indexName, indexNumber ) + " ON " + newName + " (";
//                indexNumber++;
//
//                int idx = 0;
//
//                Iterator colIterator = colMap.keySet().iterator();
//
//                String rowName = null; 
//
//                while( colIterator.hasNext() )
//                {
//                    if( idx > 0 )
//                    {
//                        sql += ",";
//                    }
//                    idx++;
//
//                    Short pos = ( Short )colIterator.next();
//                    rowName = ( String )colMap.get( pos );
//                    if( rowName.indexOf( "||" ) > 0 || rowName.indexOf( "+" ) > 0 || rowName.indexOf( "(" ) > 0 )
//                    {
//                        sql += rowName;
//                    }
//                    else
//                    {
//                        sql += connector.isPostgreSQL() ? rowName : connector.getAnalyzer().quoteIdentifier( rowName );
//                    }  
//                }
//
//                sql += " ) ";
//
//                // work around for SQL Server which doesn't allow NULLs in UNIQUE indices
//                if( idx == 1 && connector.isSQLServer() && !computedColumns.contains( rowName ) &&
//                    sql.startsWith( "CREATE UNIQUE" ) )
//                {
//                    sql = Utils.subst( sql, "CREATE UNIQUE", "CREATE UNIQUE NONCLUSTERED" );
//                    sql += "WHERE " + connector.getAnalyzer().quoteIdentifier( rowName ) + " IS NOT NULL ";
//                }
//
//                ret.add( sql );
//            }
//
//            if( connector.isOracle() )
//            {
//                String sql = "SELECT trigger_name, description, trigger_body FROM dba_triggers \n" +
//                             "WHERE table_name = UPPER('" + origName + "') AND table_owner = '" + connector.getConnectionUserName() + "'";
//                int rowNum = 0;
//
//                List<String> notCloneTriggers = Collections.emptyList();
//                String value  = Utils.getModuleSetting( connector, "attributes", "NOT_CLONE_TRIGGERS" );
//                if( value != null && value.length() > 0 )
//                {
//                    notCloneTriggers = Arrays.asList( value.split( "," ) );
//                }
//
//                for( DynamicPropertySet dps : Utils.readAsRecords( connector, sql ) )
//                {
//                    String triggerName = dps.getValueAsString( "trigger_name" );
//                    if( notCloneTriggers.contains( triggerName ) )
//                    {
//                        continue; 
//                    }
//
//                    String description = dps.getValueAsString( "description" ).replaceAll( "(?i) " + origName + " ", " " + newName + " " );
//                    String trigger_body = dps.getValueAsString( "trigger_body" ).replaceAll( "(?i) " + origName + "[\\. ]+", " " + newName + " " );
//                    description = description.replaceAll( "(?i)" + triggerName, newUniqueName( "TRG_", triggerName, rowNum++ ) );
//                    ret.add( "CREATE OR REPLACE TRIGGER " + description + " " + trigger_body );
//                }
//            }
//
//            if( connector.isSQLServer() )
//            {
//                String sql = "SELECT so.name AS \"trigger_name\", OBJECT_DEFINITION(so.ID) AS \"trigger_body\" \n";
//                sql += " FROM sysobjects so WHERE so.type = 'TR' AND OBJECT_NAME(so.parent_obj) = '" + origName + "'";
//
//                int rowNum = 0;
//
//                List<String> notCloneTriggers = Collections.emptyList();
//                String value  = Utils.getModuleSetting( connector, "attributes", "NOT_CLONE_TRIGGERS" );
//                if( value != null && value.length() > 0 )
//                {
//                    notCloneTriggers = Arrays.asList( value.split( "," ) );
//                }
//
//                for( DynamicPropertySet dps : Utils.readAsRecords( connector, sql ) )
//                {
//                    String triggerName = dps.getValueAsString( "trigger_name" );
//                    if( notCloneTriggers.contains( triggerName ) )
//                    {
//                        continue; 
//                    }
//
//                    String trigger_body = dps.getValueAsString( "trigger_body" ).trim();
//                    trigger_body = trigger_body.replaceAll( "(?i) " + origName + "[\\. \\r\\n]+", " " + newName + " " );
//                    trigger_body = trigger_body.replaceAll( "(?i)" + triggerName, newUniqueName( "TRG_", triggerName, rowNum++ ) );
//                    ret.add( trigger_body );
//                }
//            }
//
//            if( connector.isPostgreSQL() )
//            {
//                String sql = "SELECT trigger_name, action_statement, \n";
//                sql += " event_manipulation, action_orientation, action_timing \n";
//                sql += " FROM information_schema.triggers  WHERE event_object_table = LOWER('" + origName + "')";
//
//                int rowNum = 0;
//
//                List<String> notCloneTriggers = Collections.emptyList();
//                String value  = Utils.getModuleSetting( connector, "attributes", "NOT_CLONE_TRIGGERS" );
//                if( value != null && value.length() > 0 )
//                {
//                    notCloneTriggers = Arrays.asList( value.split( "," ) );
//                }
//
//                for( DynamicPropertySet dps : Utils.readAsRecords( connector, sql ) )
//                {
//                    String triggerName = dps.getValueAsString( "trigger_name" );
//                    if( notCloneTriggers.contains( triggerName ) )
//                    {
//                        continue; 
//                    }
//
//                    String trigger_body = "CREATE TRIGGER " + newUniqueName( "TRG_", triggerName, rowNum++ ) + "\n";
//                    trigger_body += dps.getValueAsString( "action_timing" ) + " " + dps.getValueAsString( "event_manipulation" ) + " ON " + newName + "\n";
//                    trigger_body += "FOR EACH " + dps.getValueAsString( "action_orientation" ) + " " + dps.getValueAsString( "action_statement" );
//                    ret.add( trigger_body );
//                }
//            }
//
//        }
//        catch( Exception e )
//        {
//            Logger.error( cat, "Can't get indexes info. ", e );
//        }
//        finally
//        {
//            connector.close( rs );
//            try
//            {
//                connector.releaseConnection( conn );
//            }
//            catch( SQLException rse )
//            {
//                Logger.error( cat, "Unable to release connection", rse );
//            }
//        }

        return ret;

    }

    @Override
    public String makeTableLikeExpr( String origName, String newName )
    {
        return makeTableLikeExpr( origName, newName, false );
    }

    @Override
    public String makeTableLikeExpr( String origName, String newName, boolean addImportFields )
    {
        return makeTableLikeExpr( connector, origName, newName, addImportFields );
    }

    /**
     * TODO puz: add PG support.
     */
    public String makeTableLikeExpr( DatabaseService srcConnector, String origName, String newName, boolean addImportFields )
    {
        Connection conn = null;
        ResultSet rs = null;

        StringBuilder sql = new StringBuilder( "CREATE TABLE " );
        sql.append( quoteUnsafeIdentifier( newName ) ).append( '(' );
//        sql.append( connector.NL() );
//
//        String importID = Utils.subst( newName, origName, "" );
//
//        DynamicPropertySet bean;
//        String pk;
//        try
//        {
//            bean = ( DynamicPropertySet )Utils.getTableBean( srcConnector, origName ).clone();
//
//            pk = Utils.findPrimaryKeyName( srcConnector, origName );
//            OperationSupport.addTagEditors( srcConnector, DatabaseConstants.PLATFORM_HTML, UserInfo.ADMIN, origName, pk, bean, new HashMap(), true, null );
//            OperationSupport.applyMetaData( srcConnector, origName, pk, bean, new HashMap(), false );
//        }
//        catch( SQLException exc )
//        {
//            Logger.error( cat, "Unable to get original info for table " + origName, exc );
//            return null;
//        }
//        catch( Exception exc )
//        {
//            Logger.error( cat, "Unable to get original info for table " + origName, exc );
//            return null;
//        }
//
//        try
//        {
//            List<String> computedColumns = Utils.readComputedColumns( srcConnector, origName );
//
//            conn = srcConnector.getConnection();
//            rs = Utils.getEntityColumnsAsResultset( srcConnector.getAnalyzer(), conn, origName );
//
//            boolean isFirst = true;
//            boolean isOracle = connector.isOracle();
//            boolean isMySQL = connector.isMySQL();
//            boolean isSQLServer = connector.isSQLServer();
//            boolean isPostgreSQL = connector.isPostgreSQL();
//
//            while( rs.next() )
//            {
//                String columnName = rs.getString( /*"COLUMN_NAME"*/ 4 );
//                String typeName = rs.getString( /*"TYPE_NAME"*/ 6 );
//                int columnSize = rs.getInt( /*"COLUMN_SIZE"*/ 7 );
//
//                boolean wasNull = false;
//                int decimalDigits = rs.getInt( /*"DECIMAL_DIGITS"*/ 9 );
//                try
//                {
//                    wasNull = rs.wasNull();
//                }
//                catch( Throwable ignore )
//                {
//                }
//                if( wasNull )
//                {
//                    decimalDigits = 0;
//                }
//
//                String colSizeStr = "" + columnSize;
//                if( decimalDigits > 0 )
//                {
//                    colSizeStr += "," + decimalDigits;
//                }
//
//                boolean canBeNull = rs.getInt( "NULLABLE" ) != ResultSetMetaData.columnNoNulls;
//                sql.append( "    " );
//                sql.append( isFirst ? ' ' : ',' );
//                sql.append( quoteUnsafeIdentifier( columnName ) ).append( ' ' );
//
//                // TODO Do something not so stupid with it
//                // bpchar is internal representation
//                if (isPostgreSQL && "bpchar".equalsIgnoreCase( typeName ))
//                {
//                    typeName = "char";
//                }
//
//                String upperType = typeName.toUpperCase();
//
//                DynamicProperty prop = bean.getProperty( columnName );
//
//                boolean isIdentity = Boolean.TRUE.equals( prop.getAttribute( JDBCRecordAdapter.AUTO_IDENTITY ) );
//                //System.out.println( "" + columnName + ".isIdentity: " + isIdentity );
//                Object defValue = prop.getAttribute( BeanInfoConstants.DEFAULT_VALUE );
//                //System.out.println( "" + columnName + ".defValue: " + defValue );
//
//                String defaultStr = "";
//                if( defValue != null )
//                {
//                    defaultStr = " DEFAULT ";
//                    if( upperType.indexOf( "CHAR" ) >= 0 || upperType.indexOf( "ENUM" ) >= 0 )
//                    {
//                        if( defValue instanceof String && !( ( String )defValue ).startsWith( "'" ) )
//                        {
//                            defaultStr += "'" + defValue + "'";
//                        }
//                        else
//                        {
//                            defaultStr += defValue;
//                        }
//                    }
//                    else
//                    {
//                        defaultStr += defValue;
//                    }
//                }
//                //System.out.println( "" + columnName + ".defaultStr: " + defaultStr );
//
//                if( defValue != null && isOracle && columnName.equalsIgnoreCase( pk ) &&
//                    (
//                            origName.equalsIgnoreCase( defValue.toString() ) ||
//                            JDBCRecordAdapter.AUTO_IDENTITY.equals( defValue )
//                    )
//                        )
//                {
//                    isIdentity = true;
//                }
//
//                boolean isMySQLUnsignedTweak = isMySQL && upperType.endsWith( " UNSIGNED" );
//
//                if( !isIdentity && !isMySQLUnsignedTweak && !computedColumns.contains( columnName ) )
//                {
//                    sql.append( typeName );
//                }
//
//                String[] tags = ( String[] )prop.getAttribute( Operation.TAG_LIST_ATTR );
//
//                if( isIdentity )
//                {
//                    if( isMySQL )
//                    {
//                        sql.append( "BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY" );
//                    }
//                    else if( isOracle )
//                    {
//                        sql.append( "VARCHAR2(15 CHAR) DEFAULT 'auto-identity' NOT NULL PRIMARY KEY" );
//                    }
//                    else if( isSQLServer )
//                    {
//                        sql.append( "BIGINT NOT NULL IDENTITY PRIMARY KEY" );
//                    }
//                    else if( isPostgreSQL )
//                    {
//                        sql.append( typeName + " PRIMARY KEY" );
//                    }
//                    else
//                    {
//                        sql.append( typeName );
//                    }
//                }
//                else if( computedColumns.contains( columnName ) && isSQLServer )
//                {
//                    String csql = "SELECT definition FROM sys.computed_columns " +
//                        "WHERE name = '" + columnName + "' AND OBJECT_ID = OBJECT_ID('" + origName + "')";
//                    sql.append( "AS " ).append( new QRec( connector, csql ).getString() );
//                }
//                else if( upperType.contains( "DATE" ) || upperType.contains( "TIME" ) )
//                {
//                    if( "DATE".equals( upperType ) && !"".equals( defaultStr ) )
//                    {
//                        if( isSQLServer )
//                        {
//                            sql.append( " DEFAULT CONVERT( DATE, '" ).append( defValue ).append( "', 120 )" );
//                        }
//                        else if( isOracle  )
//                        {
//                            sql.append( " DEFAULT TO_DATE('").append( defValue ).append( "','YYYY-MM-DD') " );
//                        }
//                    }
//                }
//                else if( Arrays.asList( "BIGINT", "INTEGER", "INT", "SMALLINT" ).contains( upperType ) )
//                {
//                }
//                else if( upperType.contains( "BIGINT" ) || upperType.contains( "INTEGER" ) )
//                {
//                }
//                else if( isMySQL && ( upperType.contains( "BLOB" ) || upperType.contains( "TEXT" ) ) )
//                {
//                }
//                else if( isSQLServer && upperType.contains( "IMAGE" ) )
//                {
//                }
//                else if( isOracle && upperType.contains( "BLOB" ) )
//                {
//                }
//                else if( isMySQLUnsignedTweak )
//                {
//                    String parts[] = typeName.split( " " );
//                    sql.append( parts[ 0 ] ).append( '(' ).append( colSizeStr ).append( ')' )
//                            .append( ' ' ).append( parts[ 1 ] ).append( defaultStr );
//                }
//                else if( tags != null )
//                {
//                    if( !upperType.contains( "ENUM" ) )
//                    {
//                        if( isOracle )
//                        {
//                            sql.append( '(' ).append( colSizeStr ).append( " CHAR)" ).append( defaultStr );
//                        }
//                        else
//                        {
//                            sql.append( '(' ).append( colSizeStr ).append( ')' ).append( defaultStr );
//                        }
//
//                        sql.append( " CHECK( " ).append( quoteUnsafeIdentifier( columnName ) ).append( " IN " );
//                    }
//                    sql.append( '(' );
//                    for( int i = 0; i < tags.length; i++ )
//                    {
//                        if( i > 0 )
//                        {
//                            sql.append( ',' );
//                        }
//                        sql.append( '\'' ).append( tags[ i ] ).append( '\'' );
//                    }
//                    sql.append( ')' );
//                    if( !upperType.contains( "ENUM" ) )
//                    {
//                        sql.append( " )" );
//                    }
//                }
//                else
//                {
//                    if( isOracle && Arrays.asList( "VARCHAR", "VARCHAR2" ).contains( upperType ) )
//                    {
//                        sql.append( '(' ).append( colSizeStr ).append( " CHAR)" ).append( defaultStr );
//                    }
//                    else if( isSQLServer && "VARCHAR".equals( upperType ) && columnSize > 8000 )
//                    {
//                        sql.append( "(MAX)" ).append( defaultStr );
//                    }
//                    else
//                    {
//                        if( isPostgreSQL )
//                        {
//                            if( Arrays.asList( "SERIAL", "BIGSERIAL" ).contains( upperType ) )
//                            {
//                                defaultStr = "";
//                            }
//                            if( Arrays.asList( "VARCHAR", "CHAR", "NUMERIC", "DECIMAL" ).contains( upperType ) )
//                            {
//                                if( "NUMERIC".equals( upperType ) && columnSize > 1000 )
//                                {
//                                    columnSize = 1000;
//                                    colSizeStr = "1000";
//                                }
//                                sql.append( '(' ).append( colSizeStr ).append( ')' );
//                            }
//                            sql.append( defaultStr );
//                        }
//                        else if( !"CLOB".equals( upperType ) )
//                        {
//                            sql.append( '(' ).append( colSizeStr ).append( ')' ).append( defaultStr );
//                        }
//                    }
//                }
//
//                if( upperType.contains( "ENUM" ) )
//                {
//                    sql.append( defaultStr );
//                }
//
//                if( !canBeNull && !isIdentity && !computedColumns.contains( columnName ) )
//                {
//                    sql.append( " NOT NULL" );
//                }
//
//                sql.append( connector.NL() );
//                isFirst = false;
//            }
//            if( addImportFields )
//            {
//                String auxFiledsType = isOracle ? "VARCHAR2" : "VARCHAR";
//                String auxFieldSizeSuffix = isOracle ? " CHAR" : "";
//
//                if( "0".equals( importID ) )
//                // here we have spcial 'restoreID' field for transport rollback
//                // in restoreID we store ID filed of backed up record
//                {
//                    if (isOracle)
//                    {
//                        // oracle has string identifiers
//                        sql.append( ",restoreID " + auxFiledsType + "(15" + auxFieldSizeSuffix + ") \n" );
//                    }
//                    else
//                    {
//                        sql.append( ",restoreID BIGINT \n" );
//                    }
//                }
//
//                if (isOracle)
//                {
//                    // oracle has string identifiers
//                    sql.append( ",origID " + auxFiledsType + "(15" + auxFieldSizeSuffix + ") \n" );
//                }
//                else
//                {
//                    sql.append( ",origID BIGINT \n" );
//                }
//                sql.append( ",linkStatus " + auxFiledsType + "(15" + auxFieldSizeSuffix + ")" );
//                sql.append( ",linkRule " + auxFiledsType + "(100" + auxFieldSizeSuffix + ")" );
//                sql.append( ",transportStatus " + auxFiledsType + "(15" + auxFieldSizeSuffix + ") DEFAULT 'fresh'" );
//            }
//        }
//        catch( Exception se )
//        {
//            Logger.error( cat, "Unable to get column list", se );
//            return null;
//        }
//        finally
//        {
//            srcConnector.close( rs );
//            try
//            {
//                srcConnector.releaseConnection( conn );
//            }
//            catch( SQLException rse )
//            {
//                Logger.error( cat, "Unable to release connection", rse );
//            }
//        }
//        sql.append( ')' );

        return sql.toString();
    }

    @Override
    public String makeVarArgsCallExpr( String function, String ... vals )
    {
        if( vals.length == 0 )
        {
            return "NULL";
        }

        if( vals.length == 1 )
        {
            return vals[ 0 ];
        }

        StringBuilder buffer = new StringBuilder();
        buffer.append( function ).append( "( " );
        for( int i = 0; i < vals.length; i++ )
        {
            if( i > 0 )
            {
                buffer.append( ", " );
            }
            buffer.append( vals[ i ] );
        }
        buffer.append( " )" );

        return buffer.toString();
    }

    @Override
    public String makeCoalesceExpr( String ... vals )
    {
        return makeVarArgsCallExpr( "COALESCE", vals );
    }

    @Override
    public String makeGreatestExpr( String ... vals )
    {
        return makeVarArgsCallExpr( "GREATEST", vals );
    }

    @Override
    public String makeLeastExpr( String ... vals )
    {
        return makeVarArgsCallExpr( "LEAST", vals );
    }

    @Override
    public String makeCastToPK( String val )
    {
        return "CAST( " + val + " AS " + getPKType() + " )";
    }

    @Override
    public String makeCastBigtextToString( String val )
    {
        return val;
    }
    
    @Override
    public String makeCastToString( String val )
    {
        return "COALESCE( CAST( " + val + " AS VARCHAR ), '' )";
    }

    @Override
    public String makeCastToString( String val, int length )
    {
        return makeCastToString( val );
    }

    @Override
    public String makeCastToInt( String val )
    {
        return "CAST( " + val + " AS BIGINT )";
    }

    @Override
    public String makeCastToCurrency( String val )
    {
        return "CAST( " + val + " AS DECIMAL( 18, 2 ) )";
    }

    @Override
    public String makeCastToDate( Date val )
    {
        if( val == null )
            return "NULL";
        return "'" + new java.sql.Date( val.getTime() ).toString() + "'";
    }

    @Override
    public String makeCastToDateExpr( String val )
    {
        throw new RuntimeException( "Not implemented. Please implement!" );
    }

    @Override
    public String makeYearExpr( String date )
    {
        throw new RuntimeException( "Not implemented. Please implement!" );
    }

    @Override
    public String makeMonthExpr( String date )
    {
        throw new RuntimeException( "Not implemented. Please implement!" );
    }

    @Override
    public String makeDayOfMonthExpr( String date )
    {
        throw new RuntimeException( "Not implemented. Please implement!" );
    }

    @Override
    public String makeFirstDayOfMonthExpr( String date )
    {
        throw new RuntimeException( "Not implemented. Please implement!" );
    }

    @Override
    public String makeLengthExpr( String columnName )
    {
        return "LENGTH(" + getCaseCorrectedIdentifier( columnName ) + ")";
    }

    @Override
    public String makeTrimExpr( String expr )
    {
        return "TRIM(" + expr + ")";
    }

    @Override
    public String makeSubstringExpr( String str, String from, String to )
    {
        return "SUBSTR( " + str + ", " + from + ", " + to + " )";
    }

    @Override
    public String makeSubstringExpr( String str, String from )
    {
        return "SUBSTR( " + str + ", " + from + " )";
    }

    @Override
    public String makeBlobLengthExpr( String columnName )
    {
        return makeLengthExpr( columnName );
    }

    @Override
    public String getCurrentDateTimeExpr()
    {
        return "'" + new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ).format( new Date() ) + "'";
    }

    @Override
    public String getCurrentDateExpr()
    {
        return "'" + new SimpleDateFormat( "yyyy-MM-dd" ).format( new Date() ) + "'";
    }

    @Override
    public String makeTruncateTableExpr( String table )
    {
        return "TRUNCATE TABLE " + quoteUnsafeIdentifier( table );
    }

    @Override
    public String makeInsertIntoWithAutoIncrement( String table, String pk )
    {
        return "INSERT INTO " + table + "( ";
    }

    @Override
    public String makeInsertValuesWithAutoIncrement()
    {
        return " VALUES( ";
    }

    @Override
    public String makeInsertAsSelectWithAutoIncrement()
    {
        return " SELECT ";
    }

    @Override
    public String makeCreatedModifiedColumnNames( DatabaseService connector, String table, boolean bAddComma )
    {
        String ret = bAddComma ? "," : "";
//        try
//        {
//            DynamicPropertySet bean = Utils.getTableBean( connector, table );
//            for( DynamicProperty prop : bean )
//            {
//                String colName = prop.getName();
//                if( !SQLHelper.SYSTEM_FIELDS.contains( colName ) || DatabaseConstants.IS_DELETED_COLUMN_NAME.equalsIgnoreCase( colName ) )
//                     continue;
//
//                ret += Arrays.asList( ",", "" ).contains( ret ) ? colName : "," + colName;
//            }
//        }
//        catch( Exception exc )
//        {
//            Logger.error( cat, "Unable to get original info for table " + table, exc );
//            return null;
//        }

        return ",".equals( ret ) ? "" : ret;
    }

    @Override
    public String makeCreatedModifiedColumnValues( DatabaseService connector, UserInfo ui, String table, boolean bAddComma )
    {
        String ret = bAddComma ? "," : "";
//        try
//        {
//            DynamicPropertySet bean = Utils.getTableBean( connector, table );
//            for( DynamicProperty prop : bean )
//            {
//                String colName = prop.getName();
//                if(
//                    colName.equalsIgnoreCase( DatabaseConstants.WHO_INSERTED_COLUMN_NAME ) ||
//                    colName.equalsIgnoreCase( DatabaseConstants.WHO_MODIFIED_COLUMN_NAME ) )
//                {
//                    ret += Arrays.asList( ",", "" ).contains( ret ) ? "" : ",";
//                    ret += Utils.safestr( connector, ui.getUserName(), true ) + " AS " + colName;
//                }
//                if(
//                    colName.equalsIgnoreCase( DatabaseConstants.CREATION_DATE_COLUMN_NAME ) ||
//                    colName.equalsIgnoreCase( DatabaseConstants.MODIFICATION_DATE_COLUMN_NAME ) )
//                {
//                    ret += Arrays.asList( ",", "" ).contains( ret ) ? "" : ",";
//                    ret += getCurrentDateTimeExpr() + " AS " + colName;
//                }
//            }
//        }
//        catch( Exception exc )
//        {
//            Logger.error( cat, "Unable to get original info for table " + table, exc );
//            return null;
//        }

        return ",".equals( ret ) ? "" : ret;
    }

    protected static final Set<String> SQLkeywords = Collections
            .unmodifiableSet(new HashSet<String>(Arrays.asList("SELECT", "KEY",
                    "ORDER", "TABLE", "WHERE", "GROUP", "FROM", "TO", "BY",
                    "JOIN", "LEFT", "INNER", "OUTER", "NUMBER", "DISTINCT",
                    "COMMENT", "START", "BEGIN", "END", "CHECK", "MODIFY",
                    "DATE", "LEVEL", "INDEX", "OUT", "USER")));

    @Override
    public boolean isSafeIdentifier( String name )
    {
        if( name == null )
        {
            return false;
        }
        if( name.startsWith( "\"" ) )
        {
            return true;
        }
        char[] chars = name.toCharArray();
        if( chars.length == 0 )
        {
            return false;
        }
        for( int i = 0; i < chars.length; i++ )
        {
            int val = chars[ i ];
            chars[ i ] = Character.toUpperCase( chars[ i ] );
            if( val >= 65 && val < 91 || val >= 97 && val < 123 )
            {
                continue;
            }
            if( ( val >= 48 && val < 58 || val == 95 ) && i > 0 )
            {
                continue;
            }
            return false;
        }
        String upperName = new String( chars );
        return !SQLkeywords.contains( upperName );
    }

    /**
     * Returns maximum length of character literal value
     * -1 means unknown (error), 0 - no limit (as returned by DB),
     * > 0 maximum length
     */
    @Override
    public int getMaxCharLiteralLength()
    {
        if ( maxCharLiteralLength > -2 )
            return maxCharLiteralLength;
        maxCharLiteralLength = -1;
//        Connection connection = null;
//        try
//        {
//            connection = connector.getConnection();
//            maxCharLiteralLength = connection.getMetaData().getMaxCharLiteralLength();
//        }
//        catch ( SQLException e )
//        {
//            Logger.error( cat, "Error getting maxCharLiteralLength", e );
//        }
//        finally
//        {
//            try
//            {
//                connector.releaseConnection( connection );
//            }
//            catch( SQLException e )
//            {
//                Logger.error( cat, "Unable to release connection", e );
//            }
//        }
        return maxCharLiteralLength;
    }

    @Override
    public String makeDateAddMillisecondsExpr( String date, String amount )
    {
        throw new RuntimeException( "Not implemented. Please implement!" );
    }

    @Override
    public String makeDateAddDaysExpr( String dateToModify, int daysToAdd )
    {
        throw new RuntimeException( "Not implemented. Please implement!" );
    }

    @Override
    public String makeDateAddMonthsExpr(String dateToModify, int monthsToAdd)
    {
        throw new RuntimeException( "Not implemented. Please implement!" );
    }

    @Override
    public String makeGenericRefExpr( String entity, String idExpr )
    {
        return makeConcatExpr( "'" + entity + ".'", makeCastToString( idExpr ) );
    }

    public String makeConcatExprBasic( String delimiter, String... vals )
    {
        return "(" + StringUtils.join( vals, " " + delimiter + " " ) + ")";
    }

    @Override
    public String makeJoinGenericRefCondition( String exprFrom, String entityTo, String exprTo )
    {
        String extract = makeSubstringExpr( exprFrom, "" + ( entityTo.length() + 2 ) );
        return " " + exprFrom + " LIKE '" + entityTo + ".%' AND " + makeCastToPK( extract ) + " = " + exprTo + " ";
    }

    protected String makeJoinGenericRefConditionAlt( String exprFrom, String entityTo, String exprTo )
    {
        return " " + exprFrom + " LIKE '" + entityTo + ".%' AND " + exprFrom + " = " + makeGenericRefExpr( entityTo, exprTo ) + " ";
    }

    @Override
    public String makeSingleExprSelect( String ... expr )
    {
        return makeSingleExprSelectBasic( "SELECT", expr );
    }

    protected String makeSingleExprSelectBasic( String clause, String ... expr )
    {
        StringBuilder s = new StringBuilder( clause );
        s.append( " (" ).append( expr[0] ).append( ")" );
        for( int i = 1; i < expr.length; i++ )
        {
            s.append( ", (" ).append( expr[i] ).append( ")" );
        }
        return s.toString();
    }

    @Override
    public String makeModExpr( String number, String divisor )
    {
        return number + " % " + divisor;
    }

    @Override
    public String getPKType()
    {
        return "BIGINT";
    }

    @Override
    public IdentifierCase getIdentifierCase()
    {
        return IdentifierCase.NEUTRAL;
    }

    @Override
    public String makeRegexpLike( String column, String pattern )
    {
        // TODO not supported
        return column + " LIKE '" + pattern + "'";
    }

    @Override
    public String makeUUID()
    {
        // TODO not supported
        return "''";
    }
    
    @Override
    public String makeLPadExpr( String origStr, String size, String padStr )
    {
        throw new UnsupportedOperationException("LPad is not implemented");
    }

    protected static String notSupportedExprDefault( String column, String platform )
    {
        StringBuilder ret = new StringBuilder();
        ret.append( "( " + column + " IS NULL OR NOT ( " );
        ret.append( column ).append( " = '" ).append( platform ).append( "' OR " );
        ret.append( column ).append( " LIKE '" ).append( platform ).append( ",%' OR " );
        ret.append( column ).append( " LIKE '%," ).append( platform ).append( "' OR " );
        ret.append( column ).append( " LIKE '%," ).append( platform ).append( ",%' " );
        ret.append( " ) )" );
        return ret.toString();
    }

    protected String getLastInsertIDFromQuery( Connection conn, String query ) throws SQLException
    {
        ResultSet rs = null;
        Statement stmt = null;
        try
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery( query );
            rs.next();
            String ret = rs.getString( 1 );
            return ret;
        }
        catch( SQLException exc )
        {
            throw exc;
        }
        finally
        {
            if( rs != null )
            {
                rs.close();
            }
            if( stmt != null )
            {
                stmt.close();
            }
        }
    }

    protected String getLastInsertIDFromQuery( String query ) throws SQLException
    {
        ResultSet rs = null;
        try
        {
            rs = connector.executeQuery( query );
            rs.next();
            String ret = rs.getString( 1 );
            return ret;
        }
        catch( SQLException exc )
        {
            throw exc;
        }
        finally
        {
            if( rs != null )
            {
                connector.close( rs );
            }
        }
    }

    protected int optimizeRecordRangeWithLimit( StringBuffer query, long startRecord, long nRecords )
    {
        if( query == null )
        {
            return 0;
        }
        if( nRecords >= Integer.MAX_VALUE )
        {
            return 0;
        }
        String tmpQuery = query.toString().trim();
//        if( Utils.isViewGlueable( tmpQuery ) )
//        {
//            return 0;
//        }
        StringTokenizer tz = new StringTokenizer( tmpQuery );
        if( tz.hasMoreTokens() && tz.nextToken( " \r\n\t\f" ).equalsIgnoreCase( "SELECT" ) )
        {
            boolean bWasFrom = false;
            while( tz.hasMoreTokens() )
            {
                String next = tz.nextToken( " \r\n\t\f" );
                if( next.equalsIgnoreCase( "FROM" ) )
                {
                    bWasFrom = true;
                    continue;
                }
                if( next.equalsIgnoreCase( "LIMIT" ) )
                {
                    return 0;
                }
            }
            if( !bWasFrom )
            {
                return 0;
            }
            query.append( " LIMIT " ).append( startRecord ).append( ',' ).append( nRecords );
            return OPTIM_OFFSET | OPTIM_LIMIT;
        }
        return 0;
    }
}
