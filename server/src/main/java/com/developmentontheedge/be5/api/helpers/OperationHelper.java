package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.CacheInfo;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Query;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.List;
import java.util.Optional;

public class OperationHelper
{
    private final Cache<String, String[][]> tagsCache;

    private final SqlService db;
    private final Meta meta;
    private final UserAwareMeta userAwareMeta;

    public static final String yes = "yes";
    public static final String no = "no";

    public OperationHelper(SqlService db, Meta meta, UserAwareMeta userAwareMeta)
    {
        this.db = db;
        this.meta = meta;
        this.userAwareMeta = userAwareMeta;

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
    public String[][] getTagsFromSelectionView(String tableName)
    {
        return tagsCache.get(tableName + "getTagsFromSelectionView" + UserInfoHolder.getLanguage(), k -> {
            Optional<Query> foundQuery = meta.findQuery(tableName, DatabaseConstants.SELECTION_VIEW);

            if (!foundQuery.isPresent())
                throw new IllegalArgumentException();

            try
            {
                List<String[]> tags = db.selectList(foundQuery.get().getQueryCompiled().validate(), rs ->
                        new String[]{rs.getString(1), rs.getString(2)}
                );
                String[][] stockArr = new String[tags.size()][2];
                return tags.toArray(stockArr);
            } catch (ProjectElementException e)
            {
                throw Be5Exception.internalInQuery(e, foundQuery.get());
            }
        });
    }

    public String[][] getTagsFromEnum(String tableName, String name)
    {
        return getTagsFromEnum(tableName, null, name);
    }

    public String[][] getTagsFromEnum(String tableName, String operationName, String name)
    {
        return tagsCache.get(tableName + "getTagsFromEnum" + operationName + "," + name + UserInfoHolder.getLanguage(), k -> {
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
        return tagsCache.get("getTagsYesNo" + UserInfoHolder.getLanguage(), k -> {
            String[][] arr = new String[2][2];
            arr[0] = new String[]{yes, userAwareMeta.getColumnTitle("query.jsp", "page", yes)};
            arr[1] = new String[]{no, userAwareMeta.getColumnTitle("query.jsp", "page", no)};
            return arr;
        });
    }

    public String[][] getTagsNoYes()
    {
        return tagsCache.get("getTagsNoYes" + UserInfoHolder.getLanguage(), k -> {
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
