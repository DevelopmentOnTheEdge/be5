package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.SqlQueryExecutor;

import java.util.Map;

public interface QueryExecutorFactory
{
    SqlQueryExecutor build(Query query, Map<String, ?> parameters);
}
