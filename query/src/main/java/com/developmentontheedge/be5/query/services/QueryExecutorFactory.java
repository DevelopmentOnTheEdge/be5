package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.impl.Be5SqlQueryExecutor;
import com.developmentontheedge.sql.format.QueryContext;

import java.util.Map;

public interface QueryExecutorFactory
{
    Be5SqlQueryExecutor build(Query query, Map<String, ?> parameters);

    Be5SqlQueryExecutor build(Query query, QueryContext queryContext);
}
