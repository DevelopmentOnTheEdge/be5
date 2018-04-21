package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.be5.api.services.Be5Caches;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.QueryService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.services.TableModelService;
import com.developmentontheedge.be5.api.sql.DpsRecordAdapter;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.query.impl.TableModel;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.QRec;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;

import com.github.benmanes.caffeine.cache.Cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class OperationHelper
{
    private final Cache<String, String[][]> tagsCache;

    private final SqlService db;
    private final Meta meta;
    private final UserAwareMeta userAwareMeta;
    private final QueryService queryService;
    private final TableModelService tableModelService;

    public static final String yes = "yes";
    public static final String no = "no";

    public OperationHelper(SqlService db, Meta meta, UserAwareMeta userAwareMeta, Be5Caches be5Caches,
                           TableModelService tableModelService, QueryService queryService)
    {
        this.db = db;
        this.meta = meta;
        this.userAwareMeta = userAwareMeta;
        this.tableModelService = tableModelService;
        this.queryService = queryService;

        tagsCache = be5Caches.createCache("Tags");
    }

//    public HashUrl createQueryUrl(Request req)
//    {
//        return new HashUrl(FrontendConstants.TABLE_ACTION, req.get(RestApiConstants.ENTITY), req.get(RestApiConstants.QUERY));
//    }

    /**
     * Creates a list of options by a table name and columns that are used for optiion value and text respectively.
     */
    public String[][] getTags(String tableName, String valueColumnName, String textColumnName)
    {
        List<String[]> tags = db.selectList("SELECT " + valueColumnName + ", " + textColumnName + " FROM " + tableName,
                rs -> new String[]{rs.getString(valueColumnName), rs.getString(textColumnName)}
        );
        String[][] stockArr = new String[tags.size()][2];

        for (int i = 0; i < tags.size(); i++) {
            stockArr[i] = new String[]{tags.get(i)[0], userAwareMeta.getColumnTitle(tableName, tags.get(i)[1])};
        }

        return stockArr;
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
    public String[][] getTagsFromSelectionView(String tableName)
    {
        return getTagsFromCustomSelectionView(tableName, DatabaseConstants.SELECTION_VIEW, Collections.emptyMap());
    }

    public String[][] getTagsFromSelectionView(String tableName, Map<String, ?> parameters)
    {
        return getTagsFromCustomSelectionView(tableName, DatabaseConstants.SELECTION_VIEW, parameters);
    }

    public String[][] getTagsFromCustomSelectionView(String tableName, String queryName)
    {
        return getTagsFromCustomSelectionView(tableName, queryName, Collections.emptyMap());
    }

    public String[][] getTagsFromCustomSelectionView(String tableName, String queryName, Map<String, ?> parameters)
    {
        Optional<Query> query = meta.findQuery(tableName, queryName);
        if (!query.isPresent())
            throw new IllegalArgumentException("Query " + tableName + "." + queryName + " not found.");

        return getTagsFromCustomSelectionView(query.get(), parameters);
    }

    public String[][] getTagsFromCustomSelectionView(Query query, Map<String, ?> parameters)
    {
        String entityName = query.getEntity().getName();
        if(query.isCacheable())
        {
            return tagsCache.get(entityName + "getTagsFromCustomSelectionView" + query.getEntity() +
                            parameters.toString() + UserInfoHolder.getLanguage(),
                    k -> getTagsFromCustomSelectionViewExecute(query, parameters)
            );
        }
        return getTagsFromCustomSelectionViewExecute(query, parameters);
    }

    public String[][] getTagsFromCustomSelectionView(String sql, Map<String, ?> parameters)
    {
        return getTagsFromCustomSelectionView(meta.createQueryFromSql(sql), parameters);
    }

    public String[][] getTagsFromQuery(String sql, Object... params)
    {
        List<String[]> tags = db.selectList(sql,
                rs -> new String[]{rs.getString(1), rs.getString(2)}, params
        );
        String[][] stockArr = new String[tags.size()][2];
        return tags.toArray(stockArr);
    }

    public Map<String, String> readAsMap( String query, Object... params )
    {
        Map<String, String> values = new LinkedHashMap<>();
        db.query(query, rs -> {
            while (rs.next())
            {
                values.put(rs.getString(1), rs.getString(2));
            }
            return null;
        }, params);
        return values;
    }
//
//    public Map<String, String> getTagsMapFromQuery( Map<String, String> parameters, String query, Object... params )
//    {
//        //return getTagsListFromQuery( Collections.emptyMap(), query, params );
//        List<String[]> tags = db.selectList("SELECT " + valueColumnName + ", " + textColumnName + " FROM " + tableName,
//                rs -> new String[]{rs.getString(valueColumnName), rs.getString(textColumnName)}
//        );
//        String[][] stockArr = new String[tags.size()][2];
//        return tags.toArray(stockArr);
//    }

    private String[][] getTagsFromCustomSelectionViewExecute(Query query, Map<String, ?> parameters)
    {
        String entityName = query.getEntity().getName();
        //todo refactoring Be5QueryExecutor,
        Map<String, String> stringStringMap = new HashMap<>();
        //parameters.forEach((key, value) -> stringStringMap.put(key, value.toString()));
        for( Map.Entry<String, ?> entry : parameters.entrySet())
        {
            if(entry.getValue() != null)stringStringMap.put(entry.getKey(), entry.getValue().toString());
        }

        TableModel tableModel;
        if(query.getType() == QueryType.GROOVY)
        {
            tableModel = tableModelService.getTableModel(query, stringStringMap);
        }
        else
        {
            tableModel = tableModelService.builder(query, stringStringMap)
                .limit(Integer.MAX_VALUE)
                .selectable(false)
                .build();
        }

        String[][] stockArr = new String[tableModel.getRows().size()][2];

        int i = 0;
        for (TableModel.RowModel row : tableModel.getRows())
        {
            String first = row.getCells().size() >= 1 ? row.getCells().get(0).content.toString() : "";
            String second = row.getCells().size() >= 2 ? row.getCells().get(1).content.toString() : "";
            stockArr[i++] = new String[]{first, userAwareMeta.getColumnTitle(entityName, second)};
        }

        return stockArr;
    }

    public String[][] localizeTags(String tableName, List<List<String> > tags)
    {
        String[][] stockArr = new String[tags.size()][2];
        tags.stream().map(tag -> new String[]{tag.get(0), tag.get(1)}).collect(Collectors.toList()).toArray(stockArr);
        return localizeTags(tableName, stockArr);
    }

    public String[][] localizeTags(String tableName, Map<String, String> tags)
    {
        String[][] stockArr = new String[tags.size()][2];
        tags.entrySet().stream().map(tag -> new String[]{tag.getKey(), tag.getValue()}).collect(Collectors.toList()).toArray(stockArr);
        return localizeTags(tableName, stockArr);
    }

    public String[][] localizeTags(String tableName, String[][] tags)
    {
        for (String[] tag : tags)
        {
            tag[1] = userAwareMeta.getColumnTitle(tableName, tag[1]);
        }

        return tags;
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

    //todo use Be5QueryExecutor?
    public List<DynamicPropertySet> readAsRecords( String sql, Object... params )
    {
        return db.selectList(sql, DpsRecordAdapter::createDps, params);
    }

    public List<DynamicPropertySet> readAsRecordsFromQuery( String sql, Map<String, ?> parameters )
    {
        return readAsRecordsFromQuery(meta.createQueryFromSql(sql), parameters);
    }

    public List<DynamicPropertySet> readAsRecordsFromQuery(String tableName, String queryName, Map<String, ?> parameters)
    {
        return readAsRecordsFromQuery(meta.getQueryIgnoringRoles(tableName, queryName), parameters);
    }

    public List<DynamicPropertySet> readAsRecordsFromQuery(Query query, Map<String, ?> parameters)
    {
        //todo refactoring Be5QueryExecutor,
        Map<String, String> stringStringMap = new HashMap<>();
        for( Map.Entry<String, ?> entry : parameters.entrySet())
        {
            if(entry.getValue() != null)stringStringMap.put(entry.getKey(), entry.getValue().toString());
        }
        return queryService.build(query, stringStringMap).execute();
    }

    public QRec readOneRecord(String sql, Map<String, ?> parameters)
    {
        return readOneRecord(meta.createQueryFromSql(sql), parameters);
    }

    public QRec readOneRecord(String tableName, String queryName, Map<String, ?> parameters)
    {
        return readOneRecord(meta.getQueryIgnoringRoles(tableName, queryName), parameters);
    }

    public QRec readOneRecord(Query query, Map<String, ?> parameters)
    {
        List<DynamicPropertySet> dpsList = readAsRecordsFromQuery(query, parameters);

        return QRec.fromList(dpsList);
    }

    public QRec qRec(String sql, Object... params)
    {
        return db.select(sql, (rs) -> DpsRecordAdapter.addDp(new QRec(), rs), params);
    }

//    public QRec withCache( String sql, Object... params )
//    {
//        throw Be5Exception.internal("not implemented");
//        //return withCache( sql, null );
//    }

    public List<List<Object>> readAsList( String sql, Object... params )
    {
        List<List<Object>> vals = new ArrayList<>();
        List<DynamicPropertySet> list = readAsRecords(sql, params);

        for (int i = 0; i < list.size(); i++)
        {
            List<Object> propertyList = new ArrayList<>();
            for (DynamicProperty property : list.get(i)) {
                propertyList.add(property.getValue());
            }
            vals.add(propertyList);
        }

        return vals;
    }

    public String[][] addTags(Map<String, String> before, String[][] tags)
    {
        Map<String, String> newTags = new LinkedHashMap<>();

        newTags.putAll(before);
        Arrays.stream(tags).forEach(tag -> newTags.put(tag[0],tag[1]));

        return toTagsArray(newTags);
    }

    public String[][] addTags(Map<String, String> before, String[][] tags, Map<String, String> after)
    {
        Map<String, String> newTags = new LinkedHashMap<>();

        newTags.putAll(before);
        Arrays.stream(tags).forEach(tag -> newTags.put(tag[0],tag[1]));
        newTags.putAll(after);

        return toTagsArray(newTags);
    }

    public String[][] toTagsArray(Map<String, String> tags)
    {
        String[][] stockArr = new String[tags.size()][2];
        tags.entrySet().stream().map(tag -> new String[]{tag.getKey(), tag.getValue()}).collect(Collectors.toList())
                .toArray(stockArr);

        return stockArr;
    }

}
