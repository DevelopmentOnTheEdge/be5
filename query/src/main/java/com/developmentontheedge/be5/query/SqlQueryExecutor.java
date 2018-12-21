package com.developmentontheedge.be5.query;

import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.sql.model.AstStart;

import java.util.List;

public interface SqlQueryExecutor extends QueryExecutor
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

    AstStart getFinalSql();
}
