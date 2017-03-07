package com.developmentontheedge.be5.api.services.impl;

import java.util.HashMap;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.services.ExecutorService;
import com.developmentontheedge.be5.api.services.QueryExecutor;
import com.developmentontheedge.be5.components.impl.model.Be5QueryExecutor;
import com.developmentontheedge.be5.metadata.model.Query;

public class ExecutorServiceImpl implements ExecutorService
{
    private final ServiceProvider serviceProvider;

    public ExecutorServiceImpl(ServiceProvider serviceProvider)
    {
        this.serviceProvider = serviceProvider;
    }

    /**
     * Creates a query executor with an empty list of parameters.
     */
    @Override
    public QueryExecutor createExecutor(Query query, Request req)
    {
        return new Be5QueryExecutor(query, new HashMap<>(), req, serviceProvider);
    }

}
