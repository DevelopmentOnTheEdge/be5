package com.developmentontheedge.be5.query;

import com.developmentontheedge.be5.query.impl.CellFormatter;
import com.developmentontheedge.be5.query.impl.QueryMetaHelper;
import com.developmentontheedge.be5.query.impl.QuerySqlGenerator;
import com.developmentontheedge.be5.query.impl.beautifiers.BeautifierCollection;
import com.developmentontheedge.be5.query.services.QueriesService;
import com.developmentontheedge.be5.query.services.QueryExecutorFactory;
import com.developmentontheedge.be5.query.services.QueryExecutorFactoryImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class QueryModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(CellFormatter.class).in(Scopes.SINGLETON);
        bind(QueryMetaHelper.class).in(Scopes.SINGLETON);
        bind(QuerySqlGenerator.class).in(Scopes.SINGLETON);
        bind(QueriesService.class).in(Scopes.SINGLETON);
        bind(BeautifierCollection.class).in(Scopes.SINGLETON);
        bind(QueryExecutorFactory.class).to(QueryExecutorFactoryImpl.class).in(Scopes.SINGLETON);
    }
}
