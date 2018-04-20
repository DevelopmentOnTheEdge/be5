package com.developmentontheedge.be5.api.services;


import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.impl.model.Be5QueryExecutor;

import java.util.Map;

public interface QueryService
{
    Be5QueryExecutor build(Query query, Map<String, String> parameters);

    Be5QueryExecutor build(Query query);
}
