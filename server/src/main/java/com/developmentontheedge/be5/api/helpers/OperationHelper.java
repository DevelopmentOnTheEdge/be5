package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.services.CacheInfo;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.components.impl.model.TableModel;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Query;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class OperationHelper
{
    private final Cache<String, String[][]> tagsCache;

    private final SqlService db;
    private final Meta meta;
    private final UserAwareMeta userAwareMeta;
    private final Injector injector;

    public static final String yes = "yes";
    public static final String no = "no";

    public OperationHelper(SqlService db, Meta meta, UserAwareMeta userAwareMeta, Injector injector)
    {
        this.db = db;
        this.meta = meta;
        this.userAwareMeta = userAwareMeta;
        this.injector = injector;

        tagsCache = Caffeine.newBuilder()
                .maximumSize(1_000)
                .recordStats()
                .build();
        CacheInfo.registerCache("Tags (dictionary)", tagsCache);
    }

//    public OperationRequest getOperationRequest(Request req)
//    {
//        return new OperationRequest(req);
//    }
//
//    public HashUrl createQueryUrl(Request req)
//    {
//        return new HashUrl(FrontendConstants.TABLE_ACTION, req.get(RestApiConstants.ENTITY), req.get(RestApiConstants.QUERY));
//    }

    /**
     * Creates a list of options by a table name and columns that are used for optiion value and text respectively.
     */
    public String[][] getTags(String tableName, String valueColumnName, String textColumnName)
    {
        return tagsCache.get(tableName+ "getTags" + valueColumnName + "," + textColumnName + UserInfoHolder.getLanguage(), k -> {
            List<String[]> tags = db.selectList("SELECT " + valueColumnName + ", " + textColumnName + " FROM " + tableName,
                    rs -> new String[]{rs.getString(valueColumnName), rs.getString(textColumnName)}
            );
            String[][] stockArr = new String[tags.size()][2];
            return tags.toArray(stockArr);
        });
    }

//    public List<Option> formOptionsWithEmptyValue(String tableName, String valueColumnName, String textColumnName, String placeholder)
//    {
//        ImmutableList.Builder<Option> options = ImmutableList.builder();
//        options.add(new Option("", placeholder));
//        options.addAll(formOptions(tableName, valueColumnName, textColumnName));
//
//        return options.build();
//    }

    /**
     * <p>
     * Creates a list of options by a specified table name. An entity with a
     * table definition for this table must be defined. A "selection view" query
     * of the entity must be defined too. Roles and visibility of the query are
     * ignored.
     * </p>
     *
     * <p>
     * A "selection view" query is a query with name "*** Selection view ***"
     * that selects rows with two fields: an identifier and a displayed text.
     * Names of columns are ignored, only the order matters.
     * </p>
     *
     * @throws IllegalArgumentException when an entity or a query is not defined
     * @throws Error if a found query cannot be compiled
     */
    public String[][] getTagsFromSelectionView(Request request, String tableName)
    {
        return getTagsFromQuery(request, tableName, DatabaseConstants.SELECTION_VIEW, new HashMap<>());
    }

    public String[][] getTagsFromSelectionView(Request request, String tableName, Map<String, String> extraParams)
    {
        //todo getTagsFromCustomSelectionView(...)
        return getTagsFromQuery(request, tableName, DatabaseConstants.SELECTION_VIEW, extraParams);
    }

//    /**
//     * Retrieves css tags for row representation. First try`s to find a selection view for specified table name, if none found, then selects
//     * from specified table all columns. And then use
//     * {@link com.beanexplorer.enterprise.OperationSupport#getTagsFromQuery(DatabaseConnector, String, String) OperationSupport.getTagsFromQuery}
//     * to find css tags.
//     *
//     * @param connector DB connector
//     * @param table table name
//     * @return array of css tags
//     * @throws Exception
//     */
//    public static String[] getTagsFromSimpleSelectionView( DatabaseConnector connector, String table )
//            throws Exception
//    {
//        return getTagsFromSimpleSelectionView( connector, table, null );
//    }
//
//    /**
//     * Retrives css tags for row representation. First try`s to find a selection view for specified table name, if none found, then selects
//     * from specified table all columns. And then use
//     * {@link com.beanexplorer.enterprise.OperationSupport#getTagsFromQuery(DatabaseConnector, String, String) OperationSupport.getTagsFromQuery}
//     * to find css tags.
//     *
//     * @param connector DB connector
//     * @param table table name
//     * @param ui user info
//     * @return array of css tags
//     * @throws Exception
//     */
//    public static String[] getTagsFromSimpleSelectionView( DatabaseConnector connector, String table, UserInfo ui )
//            throws Exception
//    {
//        String query = QueryInfo.getQueryText( connector, null, table, DatabaseConstants.SELECTION_VIEW, true );
//
//        if( query == null )
//        {
//            query = "SELECT * FROM " + table;
//        }
//        query = Utils.putRequestParameters( connector, query, new MapParamHelper( Collections.emptyMap() ),
//                ui != null ? ui : UserInfo.ADMIN );
//        return OperationSupport.getTagsFromQuery( connector, query, null );
//    }


//    /**
//     * Reads tag list from query stored in the database identified by its name
//     * for inclusion in drop down list
//     *
//     * @param connector connector to the DB
//     * @param table table name
//     * @param viewName view name
//     * @return ArrayList of style for every query row as string
//     * @throws Exception
//     */
//    public String[] getTagsFromCustomSelectionView( DatabaseConnector connector, String table, String viewName )
//            throws Exception
//    {
//        return getTagsFromCustomSelectionView( connector, table, viewName, null );
//    }
//
//    public String[] getTagsFromCustomSelectionView( DatabaseConnector connector, String table, String viewName, Map extraParams )
//            throws Exception
//    {
//        String prefix = this instanceof HttpOperationSupport ?
//                ( ( HttpOperationSupport )this ).contextPrefix :
//                null;
//        return getTagsFromCustomSelectionView( connector, prefix, userInfo, table, viewName, extraParams );
//    }
//
//    /**
//     * Gets style array, as string array, for every row query of table`s custom selection view.
//     * See also
//     * {@link #getTagsFromQuery(DatabaseConnector, String, String) getTagsFromQuery(DatabaseConnector, String, String)}.
//     *
//     * @param connector connector to the DB
//     * @param context value for context placeholder {@link com.beanexplorer.enterprise.DatabaseConstants#CONTEXT_PLACEHOLDER}
//     * @param ui user info
//     * @param table table name
//     * @param viewName view name
//     * @return ArrayList of style for every query row as string
//     * @throws Exception
//     */
//    public static String[] getTagsFromCustomSelectionView( DatabaseConnector connector,
//                                                           String context, UserInfo ui, String table, String viewName, Map<?,?> extraParams )
//            throws Exception
//    {
//        String query = QueryInfo.getQueryText( connector, table, viewName );
//
//        query = Utils.putPlaceholders( connector, query, ui, context );
//
//        if( extraParams != null )
//        {
//            extraParams = new HashMap( extraParams ); // to make it mutable
//            query = Utils.handleConditionalParts( connector, ui, query, extraParams );
//            query = Utils.putRequestParametersFromMap( connector, query, extraParams, ui );
//            if( !extraParams.isEmpty() )
//            {
//                Map realColumns = new HashMap();
//                for( Map.Entry<?,?> entry : extraParams.entrySet() )
//                {
//                    if( Utils.columnExists( connector, table, ( String )entry.getKey() ) )
//                    {
//                        realColumns.put( entry.getKey(), entry.getValue() );
//                    }
//                }
//                if( !realColumns.isEmpty() )
//                {
//                    String pk = Utils.findPrimaryKeyName( connector, table );
//                    query = Utils.addRecordFilter( connector, query, table, pk, realColumns, false, ui );
//                }
//                //System.out.println( "query = " + query );
//            }
//        }
//
//        if ( ui instanceof OperationUserInfo && ((OperationUserInfo)ui).getUnrestrictedSession() != null )
//        {
//            HttpSession session = ((OperationUserInfo)ui).getUnrestrictedSession();
//            query = Utils.putSessionVars( connector, query, session );
//            query = Utils.putDictionaryValues( connector, query, ui );
//
//            return getTagsFromQuery( connector, query, null, session );
//        }
//
//        query = Utils.putDictionaryValues( connector, query, ui );
//
//        return getTagsFromQuery( connector, query, null );
//    }

    //todo getTagsFromCustomSelectionView
    public String[][] getTagsFromQuery(Request request, String tableName, String queryName)
    {
        return getTagsFromQuery(request, tableName, queryName, new HashMap<>());
    }

    public String[][] getTagsFromQuery(Request request, String tableName, String queryName, Map<String, String> extraParams)
    {
        Optional<Query> query = meta.findQuery(tableName, queryName);
        if (!query.isPresent())
            throw new IllegalArgumentException("Query " + tableName + "." + queryName + " not found.");

        if(query.get().isCacheable())
        {
            return tagsCache.get(tableName + "getTagsFromQuery" + queryName +
                    extraParams.toString() + UserInfoHolder.getLanguage(),
                k -> getTagsFromQuery(request, tableName, query.get(), extraParams)
            );
        }
        return getTagsFromQuery(request, tableName, query.get(), extraParams);
    }

//    todo - 2 варианта - простой sql + String[] params для SqlService
//     или be-sql - полный аналог обработки be-sql из yaml
//    public String[][] getTagsFromQuery(Request request, String sql, String[] params)
//    {
//    }

    private String[][] getTagsFromQuery(Request request, String tableName, Query query, Map<String, String> extraParams)
    {
        TableModel table = TableModel
                .from(query, extraParams, request, false, injector)
                .limit(Integer.MAX_VALUE)
                .build();
        String[][] stockArr = new String[table.getRows().size()][2];

        int i = 0;
        for (TableModel.RowModel row : table.getRows())
        {
            String first = row.getCells().size() >= 1 ? row.getCells().get(0).content.toString() : "";
            String second = row.getCells().size() >= 2 ? row.getCells().get(1).content.toString() : "";
            stockArr[i++] = new String[]{first, userAwareMeta.getColumnTitle(tableName, second)};
        }

        return stockArr;
    }

    public String[][] getTagsFromEnum(String tableName, String name)
    {
        ColumnDef columnDef = meta.getColumn(tableName, name);
        if (columnDef == null) throw new IllegalArgumentException();
        return getTagsFromEnum(columnDef);
    }

    public String[][] getTagsFromEnum(ColumnDef columnDef)
    {
        String tableName = columnDef.getEntity().getName();
        return tagsCache.get(tableName + "getTagsFromEnum" + columnDef.getName() + UserInfoHolder.getLanguage(), k ->
        {
            String[] enumValues = columnDef.getType().getEnumValues();

            String[][] stockArr = new String[enumValues.length][2];

            for (int i = 0; i < enumValues.length; i++)
            {
                stockArr[i] = new String[]{enumValues[i], userAwareMeta.getColumnTitle(tableName, enumValues[i])};
            }

            return stockArr;
        });
    }

    public String[][] getTagsYesNo()
    {
        return tagsCache.get("getTagsYesNo" + UserInfoHolder.getLanguage(), k ->
        {
            String[][] arr = new String[2][2];
            arr[0] = new String[]{yes, userAwareMeta.getColumnTitle("query.jsp", "page", yes)};
            arr[1] = new String[]{no, userAwareMeta.getColumnTitle("query.jsp", "page", no)};
            return arr;
        });
    }

    public String[][] getTagsNoYes()
    {
        return tagsCache.get("getTagsNoYes" + UserInfoHolder.getLanguage(), k ->
        {
            String[][] arr = new String[2][2];
            arr[0] = new String[]{no, userAwareMeta.getColumnTitle("query.jsp", "page", no)};
            arr[1] = new String[]{yes, userAwareMeta.getColumnTitle("query.jsp", "page", yes)};
            return arr;
        });
    }
//
//    public List<Option> formOptionsWithEmptyValue(String tableName, String placeholder)
//    {
//        ImmutableList.Builder<Option> options = ImmutableList.builder();
//        options.add(new Option("", placeholder));
//        options.addAll(formOptions(tableName));
//
//        return options.build();
//    }

//
//    /**
//     * Replaces in the query {@link com.beanexplorer.enterprise.DatabaseConstants#QP_DICTIONARY_START DatabaseConstants.QP_DICTIONARY_START} placeholder
//     *
//     * <br/><br/><dictionary:entity_name [multiple="isMultiple"] [column_name1="columnfilter1_value"] [column_name2="columnfilter2_value"] .../>
//     *
//     * <br/><br/>for the set of the id`s from the specified entity_name where columns values are filtered with some specified column filter value.
//     *
//     * <br/><br/>For additional information, on how to specify filter for the column, see also
//     * {@link #addRecordFilter(DatabaseConnector, String, String, String, Map, boolean, UserInfo) addRecordFilter}
//     *
//     * @param connector DB connector
//     * @param query string to substitute {@link com.beanexplorer.enterprise.DatabaseConstants#QP_DICTIONARY_START DatabaseConstants.QP_DICTIONARY_START}
//     * placeholder for the set of values
//     * @param ui user info
//     * @return prepared query
//     */
//    public static String putDictionaryValues( DatabaseConnector connector, String query, UserInfo ui )
//    {
//        if( Utils.isEmpty( query ) )
//        {
//            return query;
//        }
//
//        try
//        {
//            while( query.indexOf( DatabaseConstants.QP_DICTIONARY_START ) != -1 )
//            {
//                String clause = getPlaceholderClause( query, DatabaseConstants.QP_DICTIONARY_START );
//                String entity = getPlaceholderName( clause, DatabaseConstants.QP_DICTIONARY_START );
//                Map attrs = new HashMap( PropertyInfo.withCache( clause ).asMap() );
//                String def = ( String )attrs.remove( DatabaseConstants.QP_DEFAULT_VALUE );
//
//                boolean bSafeValue = true;
//                if( attrs.get( DatabaseConstants.QP_SAFE_VALUE ) != null )
//                {
//                    bSafeValue = !"no".equals( attrs.remove( DatabaseConstants.QP_SAFE_VALUE ) );
//                }
//                boolean bMult = false;
//                if( bMult = ( attrs.get( DatabaseConstants.QP_DICT_PARAM_MULTIPLE ) != null ) )
//                {
//                    attrs.remove( DatabaseConstants.QP_DICT_PARAM_MULTIPLE );
//
//                }
//                if( Logger.isDebugEnabled( cat ) )
//                {
//                    Logger.debug( cat, "putDictionaryValues -> filtering values:\n" + attrs );
//                }
//
//                String pk = null;
//                if( "systemSettings".equalsIgnoreCase( entity ) )
//                {
//                    pk = "setting_value";
//                }
//                else
//                {
//                    pk = findPrimaryKeyName( connector, entity );
//                }
//
//                String replacement = null;
//
//                if( DatabaseConstants.ENTITY_TYPE_DICTIONARY.equals( getEntityType( connector, entity ) ) )
//                {
//                    List values = getValuesFromDictionary( connector, entity, pk, attrs, ui );
//                    if( values != null && !values.isEmpty() )
//                    {
//                        replacement = values.get( 0 ).toString();
//                        if( bMult )
//                        {
//                            replacement = toInClause( values, isNumericColumn( connector, entity, pk ) );
//                        }
//                        else if( !"systemSettings".equalsIgnoreCase( entity ) )
//                        {
//                            replacement = bSafeValue ? safeIdValue( connector, entity, pk, values.get( 0 ) ) : values.get( 0 ).toString();
//                        }
//                    }
//                    else
//                    {
//                        if( !isEmpty( def ) )
//                            replacement = def;
//                        else
//                            throw new Exception( "Can not get value from dictionary" );
//                    }
//                }
//                else
//                {
//                    String sql = "SELECT dict." + connector.getAnalyzer().quoteIdentifier( pk ) + " AS \"code\" ";
//                    sql += " FROM " + connector.getAnalyzer().quoteIdentifier( entity ) + " dict";
//                    sql = addRecordFilter( connector, sql, entity, pk, attrs, false, ui );
//
//                    if( Logger.isDebugEnabled( cat ) )
//                    {
//                        Logger.debug( cat, "putDictionaryValues -> SQL =\n" + sql );
//                    }
//
//                    if( bMult )
//                    {
//                        replacement = toInClause(
//                                readAsList( connector, sql, DictionaryCache.getInstance() ),
//                                isNumericColumn( connector, entity, pk )
//                        );
//                    }
//                    else
//                    {
//                        try
//                        {
//                            replacement = QRec.withCache( connector, sql, DictionaryQRecCache.getInstance() ).getString( "code" );
//                        }
//                        catch( Exception exc )
//                        {
//                            if( !isEmpty( def ) )
//                                replacement = def;
//                            else
//                                throw exc;
//                        }
//                        //if( bSafeValue )
//                        //{
//                        //    replacement = safeIdValue( connector, entity, pk, replacement );
//                        //}
//                    }
//                }
//                query = subst( query, clause, replacement );
//            }
//        }
//        catch( Exception exc )
//        {
//            Logger.error( cat, "Unable to replace 'dictionary' placeholder", exc );
//        }
//
//
//        // handle <entity:returnColumnName fied1=" ... " field2=" ... " />
//        try
//        {
//            int ind = 0;
//            while( ( ind = query.indexOf( "<", ind + 1 ) ) != -1 )
//            {
//                int colon = query.indexOf( ":", ind + 1 );
//                if( colon == -1 )
//                    continue;
//                String entity = query.substring( ind + 1, colon );
//                if( entity.indexOf( " " ) >= 0 )
//                    continue;
//                if( entity.indexOf( ">" ) >= 0 )
//                    continue;
//                String pk = null;
//                if( ( pk = findPrimaryKeyName( connector, entity ) ) == null ) // not real entity
//                    continue;
//                String clause = getPlaceholderClause( query, "<" + entity + ":" );
//                String returnColumnName = getPlaceholderName( clause, "<" + entity + ":" );
//                Map attrs = new HashMap( PropertyInfo.withCache( clause ).asMap() );
//                String def = ( String )attrs.remove( DatabaseConstants.QP_DEFAULT_VALUE );
//
//                boolean bSafeValue = true;
//                if( attrs.get( DatabaseConstants.QP_SAFE_VALUE ) != null )
//                {
//                    bSafeValue = !"no".equals( attrs.remove( DatabaseConstants.QP_SAFE_VALUE ) );
//                }
//
//                boolean bMult = false;
//                if( bMult = ( attrs.get( DatabaseConstants.QP_DICT_PARAM_MULTIPLE ) != null ) )
//                {
//                    attrs.remove( DatabaseConstants.QP_DICT_PARAM_MULTIPLE );
//                }
//
//                if( Logger.isDebugEnabled( cat ) )
//                {
//                    Logger.debug( cat, "*putDictionaryValues -> filtering values:\n" + attrs );
//                }
//
//                String replacement = null;
//
//                if( DatabaseConstants.ENTITY_TYPE_DICTIONARY.equals( getEntityType( connector, entity ) ) )
//                {
//                    List values = getValuesFromDictionary( connector, entity, returnColumnName, attrs, ui );
//                    if( values != null && !values.isEmpty() )
//                    {
//                        replacement = bMult ? toInClause( values, isNumericColumn( connector, entity, returnColumnName ) ) :
//                                ( bSafeValue ? safeIdValue( connector, entity, pk, values.get( 0 ) ) : values.get( 0 ).toString() );
//                    }
//                    else
//                    {
//                        if( !isEmpty( def ) )
//                            replacement = def;
//                        else
//                            throw new Exception( "Can not get value from dictionary " + entity + ":" + returnColumnName );
//                    }
//                }
//                else
//                {
//                    if( "systemSettings".equalsIgnoreCase( entity ) )
//                    {
//                        pk = "setting_value";
//                    }
//
//                    String sql = "SELECT dict." + returnColumnName + " AS \"val\" ";
//                    sql += " FROM " + connector.getAnalyzer().quoteIdentifier( entity ) + " dict";
//                    sql = addRecordFilter( connector, sql, entity, pk, attrs, false, ui );
//
//                    if( Logger.isDebugEnabled( cat ) )
//                    {
//                        Logger.debug( cat, "*putDictionaryValues -> SQL =\n" + sql );
//                    }
//
//                    if( bMult )
//                    {
//                        replacement = toInClause(
//                                readAsList( connector, sql, DictionaryCache.getInstance() ),
//                                isNumericColumn( connector, entity, returnColumnName )
//                        );
//                    }
//                    else
//                    {
//                        try
//                        {
//                            String value = QRec.withCache( connector, sql, DictionaryQRecCache.getInstance() ).getString( "val" );
//                            replacement = ( bSafeValue ? safeIdValue( connector, entity, returnColumnName, value ) : value );
//                        }
//                        catch( Exception exc )
//                        {
//                            if( !isEmpty( def ) )
//                                replacement = def;
//                            else
//                                throw exc;
//                        }
//                        //if( bSafeValue )
//                        //{
//                        //    replacement = safeIdValue( connector, entity, returnColumnName, replacement );
//                        //}
//                    }
//                }
//                int nDiff = clause.length() - replacement.length();
//                ind -= nDiff;
//                query = subst( query, clause, replacement );
//            }
//        }
//        catch( Exception exc )
//        {
//            Logger.error( cat, "Unable to replace 'entity:returnColumnName' placeholder.\nQuery = " + query, exc );
//        }
//
//        return query;
//    }

    //todo add helper createLabel(String text, Status status),
}
