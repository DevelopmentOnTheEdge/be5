package com.developmentontheedge.be5.query;

import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.sql.model.AstStart;
import org.apache.commons.dbutils.ResultSetHandler;

import javax.annotation.Nullable;
import java.util.List;

public interface SqlQueryExecutor extends QueryExecutor
{
    /**
     * Executes the query.
     */
    <T> List<T> list(ResultSetParser<T> parser);

    @Nullable
    <T> T query(ResultSetHandler<T> rsh);

    AstStart getFinalSql();
}
