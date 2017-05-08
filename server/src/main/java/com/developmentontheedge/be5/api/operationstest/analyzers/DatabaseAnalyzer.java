/** $Id: DatabaseAnalyzer.java,v 1.87 2014/04/25 03:52:32 lan Exp $ */

package com.developmentontheedge.be5.api.operationstest.analyzers;

import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.model.UserInfo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DatabaseAnalyzer extends java.io.Serializable
{
//    AccessibleViewsList getAccessibleViews(UserInfo ui, String context, String targetPlatform);
//
//    AccessibleOperationsList getAccessibleOperations(UserInfo ui, String context, String targetPlatform, String entity, String queryID);
//
//    CategoryNavigationList listCategoriesForNavigation(UserInfo ui, String category, String entity);
//
//    QueryInfo getQueryInfo(UserInfo ui, String context, String targetPlatform, String queryId, String tableId, String queryName, Map presetValues)
//          throws SQLException;

    Map readReferences(String context, UserInfo ui, String entity, String columnNameList) throws SQLException;

    String getCurrentDateTimeExpr();
    String getCurrentDateExpr();
    String getMinus();
    String getLastInsertID() throws SQLException;
    String getLastInsertID(Connection conn) throws SQLException;
    String getLastInsertID(Connection conn, String table, String field) throws SQLException;
    String getLastInsertID(Connection conn, String insertSQL) throws SQLException;

    String makeSoundex(String field);
    String makeCleanChars(String field, String pattern);
    String makeLevenshtein(String field1, String field2);
    String makeTableLikeExpr(String origName, String newName, boolean addImportFields);
    String makeTableLikeExpr(String origName, String newName);
    List makeIndexesLikeExpr(String origName, String newName);

    String makeMonthsDiff(Object date1, Object date2);
    String makeCoalesceExpr(String... vals);

    String makeConcatExpr(String... vals);

    String makeGreatestExpr(String... vals);
    String makeLeastExpr(String... vals);

    String makeLengthExpr(String columnName);

    String makeTrimExpr(String expr);

    String makeSubstringExpr(String str, String from, String to);
    String makeSubstringExpr(String str, String from);

    String makeDateAddMillisecondsExpr(String date, String amount);
    String makeDateAddDaysExpr(String dateToModify, int daysToAdd);
    String makeDateAddMonthsExpr(String dateToModify, int monthsToAdd);

    String makeVarArgsCallExpr(String function, String... vals);

    String makeGenericRefExpr(String entity, String idExpr);
    String makeJoinGenericRefCondition(String exprFrom, String entityTo, String exprTo);

    //added for oracle support
    String makeBlobLengthExpr(String columnName);

    String makeCastBigtextToString(String val);
    String makeCastToString(String val);
    String makeCastToString(String val, int length);
    String makeCastToInt(String val);
    String makeCastToCurrency(String val);
    String makeCastToDateExpr(String val);
    String isNumeric(String val);

    String makeCastToDate(Date val);
    String makeCastToPK(String val);
    String makeUUID();

    String makeLPadExpr(String origStr, String size, String padStr);

    String makeYearExpr(String date);
    String makeMonthExpr(String date);
    String makeDayOfMonthExpr(String date);
    String makeFirstDayOfMonthExpr(String date);

    String makeEncryptExpr(String plainText, String key);
    String makeDecryptExpr(String cipherText, String key);

    String makeTruncateTableExpr(String table);

    String makeSingleExprSelect(String... expr);

    String makeInsertIntoWithAutoIncrement(String table, String pk);
    String makeInsertValuesWithAutoIncrement();
    String makeInsertAsSelectWithAutoIncrement();
    String makeCreatedModifiedColumnNames(DatabaseService connector, String table, boolean bAddComma);
    String makeCreatedModifiedColumnValues(DatabaseService connector, UserInfo ui, String table, boolean bAddComma);

    String makeCategoryFilter(String query, String entity, String primaryKey, String category, boolean isUncategorized,
                              String classificationsTable);

    String quoteIdentifier(String identifier);
    String quoteUnsafeIdentifier(String identifier);
    boolean isSafeIdentifier(String name);
    String getCaseCorrectedIdentifier(String identifier);

    static final int OPTIM_OFFSET = 0x01;
    static final int OPTIM_LIMIT = 0x02;
    static final int OPTIM_SPEED = 0x04;

    int optimizeRecordRange(StringBuffer query, long startRecord, long nRecords);

    ResultSet explainPlan(String query) throws SQLException;

    boolean dropTableIfExists(String table);

    String getFulltextStatement(String field, String value);

    public int getMaxCharLiteralLength();
    boolean isRegexSupported();
    String makeRegexpLike(String column, String pattern);

    String makeModExpr(String number, String divisor);
    
    String getPKType();
    
    public enum IdentifierCase
    {
        NEUTRAL, UPPER, LOWER;

        public String normalize(String input)
        {
            switch(this)
            {
            case UPPER:
                return input.toUpperCase();
            case LOWER:
                return input.toLowerCase();
            default:
                return input;
            }
        }
    }
    
    IdentifierCase getIdentifierCase();
}
