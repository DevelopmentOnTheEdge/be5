package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.sql.ResultSetParser;
import com.developmentontheedge.be5.query.impl.CellFormatter;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.List;

/**
 * A way to run queries from the project model.
 * Note that a query executor has its state. Don't reuse the same executor for several requests.
 * 
 * @author asko
 */
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

    /**
     * Executes the query.
     */
    List<DynamicPropertySet> execute();
    
    List<DynamicPropertySet> execute(Object... params);

    /**
     * Executes the query for aggregate.
     */
    List<DynamicPropertySet> executeAggregate();
    
    /**
     * Executes the query.
     */
    <T> List<T> execute(ResultSetParser<T> parser, Object... params);

    /**
     * Counts the number of resulting rows.
     */
    long count();

    DynamicPropertySet getRow();

    /**
     * Returns a list of column names.
     */
    List<String> getColumnNames();

    List<DynamicPropertySet> executeSubQuery(String subqueryName, CellFormatter.VarResolver varResolver);

    void setSelectable(boolean selectable);

    int getOrderColumn();

    String getOrderDir();

    int getOffset();

    int getLimit();

    Boolean getSelectable();


    //QueryExecutor setContextApplier(ContextApplier contextApplier);
}
