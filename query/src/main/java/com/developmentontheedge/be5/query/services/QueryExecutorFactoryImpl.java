package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.GroovyRegister;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.metadata.Features;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryExecutor;
import com.developmentontheedge.be5.query.impl.Be5SqlQueryExecutor;
import com.google.inject.Injector;

import javax.inject.Inject;
import java.util.Map;

import static com.developmentontheedge.be5.metadata.MetadataUtils.getCompiledGroovyClassName;
import static com.developmentontheedge.be5.metadata.QueryType.D1;
import static com.developmentontheedge.be5.metadata.QueryType.D1_UNKNOWN;
import static com.developmentontheedge.be5.metadata.serialization.ModuleLoader2.getDevFileExists;


public class QueryExecutorFactoryImpl implements QueryExecutorFactory
{
    private final GroovyRegister groovyRegister;
    private final Injector injector;
    private final Meta meta;

    @Inject
    public QueryExecutorFactoryImpl(GroovyRegister groovyRegister, Injector injector, Meta meta)
    {
        this.groovyRegister = groovyRegister;
        this.injector = injector;
        this.meta = meta;
    }

    @Override
    public QueryExecutor get(Query query, Map<String, ?> parameters)
    {
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) parameters;

        QueryExecutor queryExecutor = getQueryExecutor(query);
        injector.injectMembers(queryExecutor);
        queryExecutor.initialize(query, params);
        return queryExecutor;
    }

    private QueryExecutor getQueryExecutor(Query query)
    {
        try
        {
            if (query.getType() == D1 || query.getType() == D1_UNKNOWN)
            {
                return new Be5SqlQueryExecutor();
            }
            else if (query.getType() == QueryType.JAVA || query.getType() == QueryType.GROOVY)
            {
                return getQueryExecutorInstance(query);
            }
            else
            {
                throw Be5Exception.internal("Unknown action type '" + query.getType() + "'");
            }
        }
        catch (RuntimeException e)
        {
            throw Be5Exception.internalInQuery(query, e);
        }
    }

    private QueryExecutor getQueryExecutorInstance(Query query)
    {
        try
        {
            switch (query.getType())
            {
                case JAVA:
                    return (QueryExecutor) Class.forName(query.getQuery()).newInstance();
                case GROOVY:
                    if (!getDevFileExists() && meta.getProject().hasFeature(Features.COMPILED_GROOVY))
                    {
                        String className = getCompiledGroovyClassName(query.getFileName());
                        return (QueryExecutor) Class.forName(className).newInstance();
                    }
                    else
                    {
                        try
                        {
                            Class aClass = groovyRegister.getClass(query.getEntity() + query.getName(),
                                    query.getQuery(), query.getFileName());

                            if (aClass != null)
                            {
                                return (QueryExecutor) aClass.newInstance();
                            }
                            else
                            {
                                throw Be5Exception.internal("Class " + query.getQuery() + " is null.");
                            }
                        }
                        catch (NoClassDefFoundError | IllegalAccessException | InstantiationException e)
                        {
                            throw new UnsupportedOperationException("Groovy feature has been excluded", e);
                        }
                    }
                default:
                    throw Be5Exception.internal("Not support operation type: " + query.getType());
            }
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException e)
        {
            throw Be5Exception.internalInQuery(query, e);
        }
    }
}
