package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.sql.ResultSetParser;
import com.developmentontheedge.be5.components.impl.model.TableModel.RawCellModel;
import com.developmentontheedge.beans.DynamicPropertySet;
import one.util.streamex.StreamEx;

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
    QueryExecutor sortOrder(int sortColumn, boolean desc);

    /**
     * Executes the query.
     */
    StreamEx<DynamicPropertySet> execute() throws Be5Exception;

    /**
     * Executes the query for aggregate.
     */
    StreamEx<DynamicPropertySet> executeAggregate() throws Be5Exception;
    
    /**
     * Executes the query.
     */
    <T> List<T> execute(ResultSetParser<T> parser) throws Be5Exception;
    
    /**
     * Executes the query. Supposes that the result value is the only column "value" in the only row.
     */
    boolean test() throws Be5Exception;
    
    /**
     * Counts the number of resulting rows.
     */
    long count() throws Be5Exception;

    DynamicPropertySet getRow();

    /**
     * Formats a cell.
     */
    Object formatCell(RawCellModel cell, DynamicPropertySet previousCells);

    /**
     * Returns a list of column names.
     */
    List<String> getColumnNames() throws Be5Exception;

}
