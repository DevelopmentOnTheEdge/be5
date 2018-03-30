package com.developmentontheedge.be5.api.services.impl;

import java.util.HashMap;

import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.services.ExecutorService;
import com.developmentontheedge.be5.api.services.QueryExecutor;
import com.developmentontheedge.be5.query.impl.model.Be5QueryExecutor;
import com.developmentontheedge.be5.metadata.model.Query;

public class ExecutorServiceImpl implements ExecutorService
{
    private final Injector injector;

    public ExecutorServiceImpl(Injector injector)
    {
        this.injector = injector;
    }

    /**
     * Creates a query executor with an empty list of parameters.
     */
    @Override
    public QueryExecutor createExecutor(Query query)
    {
        return new Be5QueryExecutor(query, new HashMap<>(), injector);
    }

}
