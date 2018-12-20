package com.developmentontheedge.be5.query;

import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.List;

public interface OrderedQueryExecutor
{
    /**
     * Sets a limit (changes state). Returns the query executor itself.
     */
    OrderedQueryExecutor limit(int limit);

    /**
     * Sets an offset (changes state). Returns the query executor itself.
     */
    OrderedQueryExecutor offset(int offset);

    /**
     * Sets sort order (changes state). Returns the query executor itself.
     */
    OrderedQueryExecutor order(int orderColumn, String orderDir);

    int getOrderColumn();

    String getOrderDir();

    int getOffset();

    int getLimit();

    Boolean isSelectable();

    /**
     * Executes the query.
     */
    List<DynamicPropertySet> execute();
}
