package com.developmentontheedge.be5.query;

import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.List;

public interface SqlQueryExecutor
{
    /**
     * Executes the query.
     */
    <T> List<T> execute(ResultSetParser<T> parser);

    <T> T getRow(ResultSetParser<T> parser);

    /**
     * Counts the number of resulting rows.
     */
    long count();

    DynamicPropertySet getRow();

    /**
     * Returns a list of column names.
     */
    List<String> getColumnNames();
}