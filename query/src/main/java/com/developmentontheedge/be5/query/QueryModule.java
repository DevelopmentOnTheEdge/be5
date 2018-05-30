package com.developmentontheedge.be5.query;

import com.developmentontheedge.be5.query.services.QueryService;
import com.developmentontheedge.be5.query.services.impl.QueryServiceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;


public class QueryModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(QueryService.class).to(QueryServiceImpl.class).in(Scopes.SINGLETON);
    }
}
