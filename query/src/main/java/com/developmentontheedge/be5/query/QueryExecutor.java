package com.developmentontheedge.be5.query;

import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.List;

public interface QueryExecutor
{
    /**
     * Sets a limit (changes state). Returns the query executor itself.
     */
    QueryExecutor limit(int limit);

    /**
     * Sets an offset (changes state). Returns the query executor itself.
     */
    QueryExecutor offset(int offset);

    /**
     * Sets sort order (changes state). Returns the query executor itself.
     */
    QueryExecutor order(int orderColumn, String orderDir);

    int getOrderColumn();

    String getOrderDir();

    int getOffset();

    int getLimit();

    Boolean isSelectable();

    /**
     * Executes the query.
     */
    List<DynamicPropertySet> execute();

    /**
     * Counts the number of resulting rows.
     */
    long count();
}
