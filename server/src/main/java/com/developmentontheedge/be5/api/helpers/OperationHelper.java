package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.model.Query;

import java.util.List;
import java.util.Optional;

public class OperationHelper
{
    private final SqlService db;
    private final Meta meta;

    public OperationHelper(SqlService db, Meta meta)
    {
        this.db = db;
        this.meta = meta;
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
        List<String[]> tags = db.selectList("SELECT "+valueColumnName+", "+textColumnName+" FROM " + tableName,
                rs -> new String[]{rs.getString(valueColumnName), rs.getString(textColumnName)}
        );
        String[][] stockArr = new String[tags.size()][2];
        return tags.toArray(stockArr);
    }

    //TODO
    //TAG_LIST_ATTR: [ 'yes', 'no' ]
    //TAG_LIST_ATTR: [ 'no', 'yes' ]

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
        }
        catch (ProjectElementException e)
        {
            throw Be5Exception.internalInQuery(e, foundQuery.get());
        }
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
