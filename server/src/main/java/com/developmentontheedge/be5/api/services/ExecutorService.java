package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.metadata.model.Query;

public interface ExecutorService
{

    /**
     * Creates an executor for the query.
     */
    QueryExecutor createExecutor(Query query, Request req);

}
