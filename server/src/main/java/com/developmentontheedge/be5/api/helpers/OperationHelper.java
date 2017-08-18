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
        return getTagsFromQuery(request, tableName, DatabaseConstants.SELECTION_VIEW, extraParams);
    }

    public String[][] getTagsFromQuery(Request request, String tableName, String queryName)
    {
        return getTagsFromQuery(request, tableName, queryName, new HashMap<>());
    }

    public String[][] getTagsFromQuery(Request request, String tableName, String queryName, Map<String, String> extraParams)
    {
        Optional<Query> query = meta.findQuery(tableName, queryName);
        if (!query.isPresent())
            throw new IllegalArgumentException();

        if(query.get().isCacheable())
        {
            return tagsCache.get(tableName + "getTagsFromQuery" + queryName +
                    extraParams.toString() + UserInfoHolder.getLanguage(),
                k -> getTagsFromQuery(request, tableName, query.get(), extraParams)
            );
        }
        return getTagsFromQuery(request, tableName, query.get(), extraParams);
    }

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
            stockArr[i++] = new String[]{first, userAwareMeta.getLocalizedOperationField(tableName, second)};
        }

        return stockArr;
    }

    public String[][] getTagsFromEnum(String tableName, String name)
    {
        return getTagsFromEnum(tableName, null, name);
    }

    public String[][] getTagsFromEnum(String tableName, String operationName, String name)
    {
        return tagsCache.get(tableName + "getTagsFromEnum" + operationName + "," + name + UserInfoHolder.getLanguage(), k ->
        {
            ColumnDef column = meta.getColumn(tableName, name);

            if (column == null) throw new IllegalArgumentException();
            String[] enumValues = column.getType().getEnumValues();

            String[][] stockArr = new String[enumValues.length][2];

            for (int i = 0; i < enumValues.length; i++)
            {
                if (operationName != null)
                {
                    stockArr[i] = new String[]{enumValues[i], userAwareMeta.getLocalizedOperationField(tableName, operationName, enumValues[i])};
                } else
                {
                    stockArr[i] = new String[]{enumValues[i], userAwareMeta.getLocalizedOperationField(tableName, enumValues[i])};
                }
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

}
