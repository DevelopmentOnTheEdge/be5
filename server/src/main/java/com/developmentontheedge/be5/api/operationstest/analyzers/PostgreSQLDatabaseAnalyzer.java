package com.developmentontheedge.be5.api.operationstest.analyzers;

import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.metadata.util.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * 
 * @author puz
 *
 */
public class PostgreSQLDatabaseAnalyzer extends DatabaseAnalyzerSupport
{
    private static final Logger log = Logger.getLogger(PostgreSQLDatabaseAnalyzer.class.getName());

    public PostgreSQLDatabaseAnalyzer(DatabaseService connector)
    {
        super( connector );
//        AccessibleViewsListClass = PostgreSQLAccessibleViewsList.class;
//        AccessibleOperationsListClass = PostgreSQLAccessibleOperationsList.class;
//        QueryInfoClass = PostgreSQLQueryInfo.class;
//        CategoryNavigationListClass = PostgreSQLCategoryNavigationList.class;
    }

    @Override
    public String getCurrentDateTimeExpr()
    {
        return "NOW()";
    }

    @Override
    public String getCurrentDateExpr()
    {
        return "NOW()";
    }

    @Override
    public String makeLevenshtein( String field1, String field2 )
    {
        //return "LEVENSHTEIN(" + field1 + ", " + field2 + ")" ;
        return "CASE WHEN " + field1 + " = " + field2 + " THEN 0 ELSE 99 END" ;
    }

    @Override
    public String makeDateAddMillisecondsExpr( String date, String amount )
    {
        return "(" + date + " + INTERVAL '1 MILLISECOND' * (" +  amount + "))";
    }


    @Override
    public String makeDateAddDaysExpr( String dateToModify, int daysToAdd )
    {
        return  "(" + dateToModify + " + INTERVAL '1 DAY' * (" +  daysToAdd + "))";
    }

    /**
     * In postgresql if you add interval ( month, etc )
     * to date, or timestamp you always get a timestamp
     * @param dateToModify
     * @param monthsToAdd
     * @return
     */
    @Override
    public String makeDateAddMonthsExpr(String dateToModify, int monthsToAdd) {
        if(StringUtils.isEmpty(dateToModify) )
        {
            throw new RuntimeException( "you cannot add months to an empty date" );
        }
        return  "(" + dateToModify + " + INTERVAL '1 MONTH' * (" +  monthsToAdd + "))";
    }

    private boolean isSequenceExists( Connection conn, String table, String field ) throws SQLException
    {
        String checkSeq = "SELECT COUNT( 1 ) AS \"cnt\" FROM information_schema.sequences WHERE sequence_name='" + table.toLowerCase() + "_" + field.toLowerCase() + "_seq'";
        try( PreparedStatement st = conn.prepareStatement( checkSeq );
             ResultSet rs = st.executeQuery() )
        {
            return rs.next() && rs.getInt( "cnt" ) == 1;
        }
    }

    @Override
    public String getLastInsertID( Connection conn, String table, String field ) throws SQLException
    {
        if( !StringUtils.isEmpty( table ) && !StringUtils.isEmpty( field ) && isSequenceExists( conn, table, field ) )
        {
            return getLastInsertIDFromQuery( conn, "SELECT CURRVAL ('" + table + "_" + field + "_seq') AS \"lid\"" );
        }
        return null;
    }

    @Override
    public String getLastInsertID( Connection conn, String insertSQL ) throws SQLException
    {
//        final String table = null;//Utils.getTableName( insertSQL );
//        if( !StringUtils.isEmpty( table ) )
//        {
//            String keyName = null;
//            try
//            {
//                // TODO how to use connector with conn ?
//                // is it a problem?
//                keyName = Utils.findPrimaryKeyName( connector, table );
//                if( keyName == null && table.matches( "[a-zA-Z]+[0-9]+$" ) )
//                {
//                    Matcher matcher = Pattern.compile( "[a-zA-Z]+([0-9]+)$" ).matcher( table );
//                    if( matcher.matches() )
//                    {
//                        String cloneID = matcher.group( 1 );
//                        String clonedTable = table.substring( 0, table.length() - cloneID.length() );
//                        keyName = Utils.findPrimaryKeyName( connector, clonedTable );
//                        Utils.singleLog( "INFO", cat, "No primary key found for '" + table + "', tried '" + clonedTable + "' and found: " + keyName );
//                    }
//                }
//                else if( keyName == null )
//                {
//                     ResultSet rs = null;
//                     try
//                     {
//                         rs = Utils.getEntityColumnsAsResultset( this, conn, table );
//                         if( rs.next() )
//                         {
//                             keyName = rs.getString( 4 ); //rs.getString( "COLUMN_NAME" );
//                         }
//                     }
//                     finally
//                     {
//                         connector.close( rs );
//                     }
//                }
//            }
//            catch( Exception e )
//            {
//                log.severe( "When finding primaryKey");
//                e.printStackTrace();
//            }
//
//            if( !StringUtils.isEmpty( keyName ) )
//            {
//                return getLastInsertID(conn, table, keyName);
//            }
//        }

        return null;
    }


    @Override
    public String getLastInsertID(Connection conn) throws SQLException
    {
        throw new SQLException( "getLastInsertID(Connection) is not supported, "
                + "use getLastInsertID(Connection conn, String table, String field) instead" );
    }

    @Override
    public String getLastInsertID() throws SQLException
    {
        throw new SQLException( "getLastInsertID() is not supported, use "
                + "getLastInsertID(Connection conn, String table, String field) instead" );
    }

    @Override
    public String makeMonthsDiff( Object date1, Object date2 )
    {
        return "TO_NUMBER(TO_CHAR(" + date2 + "::date,'MM'),'99')-TO_NUMBER(TO_CHAR(" + date1 + "::date,'MM'),'99')";
    }

    @Override
    public String makeGenericRefExpr( String entity, String idExpr )
    {
        return makeConcatExpr( "('" + entity + ".'", "CAST( " + idExpr + " AS VARCHAR ) )" );
    }

    @Override
    public String makeJoinGenericRefCondition( String exprFrom, String entityTo, String exprTo )
    {
        return makeJoinGenericRefConditionAlt( exprFrom, entityTo, exprTo );
    }

    @Override
    public String makeConcatExpr(String... vals)
    {
        return makeConcatExprBasic( "||", vals );
    }

    @Override
    public String makeCastToDate( java.util.Date val )
    {
        if( val == null )
            return "NULL";
        return "TO_DATE( '" + new java.sql.Date( val.getTime() ).toString() + "', 'YYYY-MM-DD' )";
    }

    @Override
    public String makeCastToDateExpr( String val )
    {
        return "TO_DATE( CAST( " + val + " AS VARCHAR ), 'YYYY-MM-DD' )";
    }

    @Override
    public String isNumeric( String val )
    {
        return val + " ~ '^[0-9]+$'";
    }

    @Override
    public String makeYearExpr( String date )
    {
        return "CAST( EXTRACT( YEAR FROM " + date + " ) AS INT)";
    }

    @Override
    public String makeMonthExpr( String date )
    {
        return "CAST( EXTRACT( MONTH FROM " + date + " ) AS INT)";
    }

    @Override
    public String makeDayOfMonthExpr( String date )
    {
        return "CAST( EXTRACT( DAYS FROM " + date + " ) AS INT)";
    }

    @Override
    public String makeFirstDayOfMonthExpr( String date )
    {
        return "DATE_TRUNC('MONTH'," + date + ")";
    }

    @Override
    public String makeEncryptExpr(String plainText, String key)
    {
        return null;
    }

    @Override
    public String makeDecryptExpr(String cipherText, String key)
    {
        return null;
    }

    @Override
    public ResultSet explainPlan(String query) throws SQLException
    {
        return connector.executeQuery( "EXPLAIN " + query );
    }

    @Override
    public String quoteIdentifier(String identifier)
    {
        if( !isSafeIdentifier( identifier ) )
        {
            return "\"" + identifier + "\"";
        }
        return identifier;
    }

    @Override
    public int optimizeRecordRange(StringBuffer query, long startRecord, long nRecords)
    {
        if( query == null )
        {
            return 0;
        }
        if( nRecords >= Integer.MAX_VALUE )
        {
            return 0;
        }
//        String tmpQuery = query.toString().trim();
//        if( Utils.isViewGlueable( tmpQuery ) )
//        {
//            return 0;
//        }

        // FIXME Preserve ordering!!!
        StringBuilder sb = new StringBuilder( query.length() + 100 );
        sb.append( "SELECT * FROM ( \n" );
        sb.append( query );
        sb.append( "\n ) x \n" );
        sb.append( " LIMIT " ).append( nRecords ).append( " OFFSET " ).append( startRecord );

        query.delete( 0, query.length() );
        query.insert( 0, sb );

        return OPTIM_OFFSET | OPTIM_LIMIT;
    }

    /**
     * WUT?
     *
     * TODO What it does?
     *
     * @param column
     * @param platform
     * @return
     */
    private static String notSupportedExpr(String column, String platform)
    {
        return notSupportedExprDefault( column, platform );
    }

//    public static class PostgreSQLAccessibleViewsList extends AccessibleViewsList implements DatabaseConstants
//    {
//        public PostgreSQLAccessibleViewsList(DatabaseService connector, DatabaseAnalyzerSupport an,
//                                             UserInfo ui, String context, String targetPlatform)
//        {
//            super( connector, an, ui, context, targetPlatform );
//        }
//    }
//
//
//    public static class PostgreSQLAccessibleOperationsList extends AccessibleOperationsList
//    {
//        public PostgreSQLAccessibleOperationsList(DatabaseService connector, DatabaseAnalyzerSupport an,
//             UserInfo ui, String context, String targetPlatform,
//             String entity, String queryID)
//        {
//            super( connector, an, ui, context, targetPlatform, entity, queryID );
//        }
//    }
//
//    public static class PostgreSQLQueryInfo extends QueryInfo
//    {
//        public PostgreSQLQueryInfo(DatabaseService connector, UserInfo ui, String context, String targetPlatform, String queryId,
//                String tableId, String queryName, Map presetValues) throws SQLException
//        {
//            super( connector, ui, context, targetPlatform, queryId, tableId, queryName, presetValues );
//        }
//    }
//
//    public static class PostgreSQLCategoryNavigationList extends CategoryNavigationList
//    {
//        public PostgreSQLCategoryNavigationList(DatabaseService connector, UserInfo ui, String category, String entity)
//        {
//            super( connector, ui, category, entity );
//        }
//    }

    @Override
    public boolean dropTableIfExists( String table )
    {
        try
        {
            connector.executeUpdate( "DROP TABLE IF EXISTS " + table );
        }
        catch( SQLException exc )
        {
            log.warning( "Unable to drop table \"" + table + "\", reason: " + exc.getMessage() );
            return false;
        }
        return true;
    }


    @Override
    public String getFulltextStatement(String field, String value)
    {
        return "to_tsquery('" + value + "') @@ to_tsvector(" + field + ")";
    }

    @Override
    public boolean isRegexSupported()
    {
        return true;
    }

    @Override
    public boolean isSafeIdentifier(String name)
    {
        return super.isSafeIdentifier( name ) && !name.startsWith( "_" ) && !name.equalsIgnoreCase( "COLUMN" );
    }

    @Override
    public IdentifierCase getIdentifierCase()
    {
        return IdentifierCase.LOWER;
    }

    @Override
    public String makeRegexpLike( String column, String pattern )
    {
        return column + " ~ '" + pattern + "'";
    }

    @Override
    public String makeUUID()
    {
        return "UUID_GENERATE_V4()";
    }

    @Override
    public String makeLPadExpr( String origStr, String size, String padStr )
    {
        return "LPAD(" + origStr + "," + size + "," + padStr + ")";
    }
}
