package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryExecutor;

import java.util.Map;

public interface QueryExecutorFactory
{
    QueryExecutor get(Query query, Map<String, ?> parameters);
}
