package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.cache.Be5Caches;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.meta.UserAwareMeta;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.impl.QuerySqlGenerator;
import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.query.sql.QRecParser;
import com.developmentontheedge.be5.security.UserInfoProvider;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.github.benmanes.caffeine.cache.Cache;
import org.apache.commons.dbutils.ResultSetHandler;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.HIDDEN_COLUMN_PREFIX;
import static com.developmentontheedge.be5.metadata.model.SqlBoolColumnType.NO;
import static com.developmentontheedge.be5.metadata.model.SqlBoolColumnType.YES;


public class QueriesService
{
    private final Cache<String, String[][]> tagsCache;

    private final DbService db;
    private final Meta meta;
    private final UserAwareMeta userAwareMeta;
    private final QuerySqlGenerator querySqlGenerator;
    private final QueryExecutorFactory queryExecutorFactory;
    private final UserInfoProvider userInfoProvider;

    @Inject
    public QueriesService(DbService db, Meta meta, UserAwareMeta userAwareMeta, Be5Caches be5Caches,
                          QuerySqlGenerator querySqlGenerator, QueryExecutorFactory queryExecutorFactory,
                          UserInfoProvider userInfoProvider)
    {
        this.db = db;
        this.meta = meta;
        this.userAwareMeta = userAwareMeta;
        this.querySqlGenerator = querySqlGenerator;
        this.queryExecutorFactory = queryExecutorFactory;

        tagsCache = be5Caches.createCache("Tags");
        this.userInfoProvider = userInfoProvider;
    }

    /**
     * Creates a list of options by a table name and columns that are used for optiion value and text respectively.
     */
    public String[][] getTags(String tableName, String valueColumnName, String textColumnName)
    {
        List<String[]> tags = db.list("SELECT " + valueColumnName + ", " + textColumnName + " FROM " + tableName,
                rs -> new String[]{rs.getString(valueColumnName), rs.getString(textColumnName)}
        );
        String[][] stockArr = new String[tags.size()][2];

        for (int i = 0; i < tags.size(); i++)
        {
            stockArr[i] = new String[]{tags.get(i)[0], userAwareMeta.getColumnTitle(tableName, tags.get(i)[1])};
        }

        return stockArr;
    }

//    public List<Option> formOptionsWithEmptyValue(String tableName, String valueColumnName, String textColumnName,
// String placeholder)
//    {
//        ImmutableList.Builder<Option> options = ImmutableList.builder();
//        options.add(new Option("", placeholder));
//        options.addAll(formOptions(tableName, valueColumnName, textColumnName));
//
//        return options.build();
//    }

    /**
     * Creates a list of options by a specified table name. An entity with a
     * table definition for this table must be defined. A "selection view" query
     * of the entity must be defined too. Roles and visibility of the query are
     * ignored.
     * </p>
     * A "selection view" query is a query with name "*** Selection view ***"
     * that selects rows with two fields: an identifier and a displayed text.
     * Names of columns are ignored, only the order matters.
     * </p>
     *
     * @throws IllegalArgumentException when an entity or a query is not defined
     * @throws Error                    if a found query cannot be compiled
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
        return getTagsFromCustomSelectionView(meta.getQuery(tableName, queryName), parameters);
    }

    public String[][] getTagsFromCustomSelectionView(Query query, Map<String, ?> parameters)
    {
        String entityName = query.getEntity().getName();
        if (query.isCacheable())
        {
            return tagsCache.get(entityName + "getTagsFromCustomSelectionView" + query.getEntity() +
                            parameters.toString() + userInfoProvider.getLanguage(),
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
        List<String[]> tags = db.list(sql,
                rs -> new String[]{rs.getString(1), rs.getString(2)}, params
        );
        String[][] stockArr = new String[tags.size()][2];
        return tags.toArray(stockArr);
    }

//
//    public Map<String, String> getTagsMapFromQuery( Map<String, Object> parameters, String query, Object... params )
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
        List<QRec> list = getQueryRows(query, parameters);
        String[][] stockArr = new String[list.size()][2];

        int i = 0;
        for (DynamicPropertySet dps : list)
        {
            Iterator<DynamicProperty> iterator = dps.iterator();
            iterator.hasNext();
            DynamicProperty property = iterator.next();
            if (property.getName().startsWith(HIDDEN_COLUMN_PREFIX))
            {
                iterator.hasNext();
                property = iterator.next();
            }
            String key = property.getValue().toString();
            Object value = iterator.hasNext() ? iterator.next().getValue() : "";
            String second = value != null ? value.toString() : "";
            stockArr[i++] = new String[]{key, userAwareMeta.getColumnTitle(query.getEntity().getName(), second)};
        }

        return stockArr;
    }

    public String[][] localizeTags(String tableName, List<List<String>> tags)
    {
        String[][] stockArr = new String[tags.size()][2];
        tags.stream().map(tag -> new String[]{tag.get(0), tag.get(1)}).collect(Collectors.toList()).toArray(stockArr);
        return localizeTags(tableName, stockArr);
    }

    public String[][] localizeTags(String tableName, Map<String, String> tags)
    {
        String[][] stockArr = new String[tags.size()][2];
        tags.entrySet().stream()
                .map(tag -> new String[]{tag.getKey(), tag.getValue()})
                .collect(Collectors.toList())
                .toArray(stockArr);
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

    public String[][] localizeTags(String tableName, String queryName, String[][] tags)
    {
        for (String[] tag : tags)
        {
            tag[1] = userAwareMeta.getColumnTitle(tableName, queryName, tag[1]);
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
        String key = tableName + "getTagsFromEnum" + columnDef.getName() + userInfoProvider.getLanguage();
        return tagsCache.get(key, k ->
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
        return tagsCache.get("getTagsYesNo" + userInfoProvider.getLanguage(), k ->
        {
            String[][] arr = new String[2][2];
            arr[0] = new String[]{YES, userAwareMeta.getColumnTitle("query.jsp", "page", YES)};
            arr[1] = new String[]{NO, userAwareMeta.getColumnTitle("query.jsp", "page", NO)};
            return arr;
        });
    }

    public String[][] getTagsNoYes()
    {
        return tagsCache.get("getTagsNoYes" + userInfoProvider.getLanguage(), k ->
        {
            String[][] arr = new String[2][2];
            arr[0] = new String[]{NO, userAwareMeta.getColumnTitle("query.jsp", "page", NO)};
            arr[1] = new String[]{YES, userAwareMeta.getColumnTitle("query.jsp", "page", YES)};
            return arr;
        });
    }

    public String[][] addTags(Map<String, String> before, String[][] tags)
    {
        Map<String, String> newTags = new LinkedHashMap<>();

        newTags.putAll(before);
        Arrays.stream(tags).forEach(tag -> newTags.put(tag[0], tag[1]));

        return toTagsArray(newTags);
    }

    public String[][] addPrefix(String[][] tags, String prefix)
    {
        String[][] stockArr = new String[tags.length][2];
        for (int i = 0; i < stockArr.length; i++)
        {
            stockArr[i] = new String[]{prefix + tags[i][0], tags[i][1]};
        }
        return stockArr;
    }

    public String[][] addTags(Map<String, String> before, String[][] tags, Map<String, String> after)
    {
        Map<String, String> newTags = new LinkedHashMap<>();

        newTags.putAll(before);
        Arrays.stream(tags).forEach(tag -> newTags.put(tag[0], tag[1]));
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

    public List<QRec> readAsRecords(String sql, Object... params)
    {
        return db.list(sql, new QRecParser(), params);
    }

    public QRec qRec(String sql, Object... params)
    {
        return db.select(sql, new QRecParser(), params);
    }

/* TODO add
    public <T> List<T> scalarList(String tableName, String queryName, Map<String, ?> parameters)
    {
        return list(meta.getQuery(tableName, queryName), new ScalarParser<T>(), parameters);
    }

    public List<Long> scalarLongList(String tableName, String queryName, Map<String, ?> parameters)
    {
        return list(meta.getQuery(tableName, queryName), new ScalarLongParser(), parameters);
    }
*/
    public List<List<Object>> listOfLists(String sql, Object... params)
    {
        List<List<Object>> vals = new ArrayList<>();
        List<QRec> list = readAsRecords(sql, params);

        for (QRec aList : list)
        {
            List<Object> propertyList = new ArrayList<>();
            for (DynamicProperty property : aList)
            {
                propertyList.add(property.getValue());
            }
            vals.add(propertyList);
        }

        return vals;
    }

    public Map<String, Object> readAsMap(String query, Object... params)
    {
        Map<String, Object> values = new LinkedHashMap<>();
        db.query(query, rs -> {
            while (rs.next())
            {
                values.put(rs.getString(1), rs.getObject(2));
            }
            return null;
        }, params);
        return values;
    }

    public List<QRec> query(String sql, Map<String, ?> parameters)
    {
        return query(meta.createQueryFromSql(sql), parameters);
    }

    public List<QRec> query(String tableName, String queryName, Map<String, ?> parameters)
    {
        return query(meta.getQuery(tableName, queryName), parameters);
    }

    public List<QRec> query(Query query, Map<String, ?> parameters)
    {
        return getQueryRows(query, parameters);
    }

    public QRec queryRecord(String sql, Map<String, ?> parameters)
    {
        return queryRecord(meta.createQueryFromSql(sql), parameters);
    }

    public QRec queryRecord(String tableName, String queryName, Map<String, ?> parameters)
    {
        return queryRecord(meta.getQuery(tableName, queryName), parameters);
    }

    public QRec queryRecord(Query query, Map<String, ?> parameters)
    {
        List<QRec> list = getQueryRows(query, parameters);
        if (list.size() == 0)
        {
            return null;
        }
        else
        {
            return list.get(0);
        }
    }

    @Nullable
    public <T> T query(String tableName, String queryName, Map<String, ?> parameters, ResultSetHandler<T> rsh)
    {
        Query query = meta.getQuery(tableName, queryName);
        String sql = querySqlGenerator.getSql(query, parameters).format();
        return db.query(sql, rsh);
    }

    public <T> List<T> list(String tableName, String queryName, Map<String, ?> parameters, ResultSetParser<T> parser)
    {
        return list(meta.getQuery(tableName, queryName), parameters, parser);
    }

    public <T> List<T> list(String sql, Map<String, ?> parameters, ResultSetParser<T> parser)
    {
        return list(meta.createQueryFromSql(sql), parameters, parser);
    }

    public <T> List<T> list(Query query, Map<String, ?> parameters, ResultSetParser<T> parser)
    {
        String sql = querySqlGenerator.getSql(query, parameters).format();
        return db.list(sql, parser);
    }

    @Nullable
    public <T> T one(String tableName, String queryName, Map<String, ?> parameters)
    {
        Query query = meta.getQuery(tableName, queryName);
        String sql = querySqlGenerator.getSql(query, parameters).format();
        return db.one(sql);
    }

    private List<QRec> getQueryRows(Query query, Map<String, ?> newParams)
    {
        try
        {
            return queryExecutorFactory.get(query, newParams).execute();
        }
        catch (RuntimeException e)
        {
            throw Be5Exception.internalInQuery(query, e);
        }
    }
}
