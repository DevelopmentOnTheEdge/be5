package com.developmentontheedge.be5.query;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.List;
import java.util.Map;


public interface QueryExecutor extends OrderedQueryExecutor
{
    QueryExecutor initialize(Query query, Map<String, Object> parameters);

    List<DynamicPropertySet> execute();
}
