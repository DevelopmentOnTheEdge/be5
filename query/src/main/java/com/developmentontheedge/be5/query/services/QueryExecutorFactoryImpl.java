package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.GroovyRegister;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.metadata.Features;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryExecutor;
import com.developmentontheedge.be5.query.impl.Be5SqlQueryExecutor;
import com.developmentontheedge.be5.query.support.AbstractQueryExecutor;
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
        try
        {
            if (query.getType() == D1 || query.getType() == D1_UNKNOWN)
            {
                return getSqlQueryExecutor(query, parameters);
            }
            else if (query.getType() == QueryType.JAVA || query.getType() == QueryType.GROOVY)
            {
                return getQueryExecutor(query, parameters);
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

    private QueryExecutor getSqlQueryExecutor(Query query, Map<String, ?> parameters)
    {
        Be5SqlQueryExecutor be5SqlQueryExecutor = new Be5SqlQueryExecutor();
        return getInitialized(be5SqlQueryExecutor, query, parameters);
    }

    private QueryExecutor getQueryExecutor(Query query, Map<String, ?> parameters)
    {
        AbstractQueryExecutor abstractQueryExecutor;

        try
        {
            switch (query.getType())
            {
                case JAVA:
                    abstractQueryExecutor = (AbstractQueryExecutor) Class.forName(query.getQuery()).newInstance();
                    break;
                case GROOVY:
                    if (!getDevFileExists() && meta.getProject().hasFeature(Features.COMPILED_GROOVY))
                    {
                        String className = getCompiledGroovyClassName(query.getFileName());
                        abstractQueryExecutor = (AbstractQueryExecutor) Class.forName(className).newInstance();
                    }
                    else
                    {
                        try
                        {
                            Class aClass = groovyRegister.getClass(query.getEntity() + query.getName(),
                                    query.getQuery(), query.getFileName());

                            if (aClass != null)
                            {
                                abstractQueryExecutor = (AbstractQueryExecutor) aClass.newInstance();
                                break;
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
                    break;
                default:
                    throw Be5Exception.internal("Not support operation type: " + query.getType());
            }
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException e)
        {
            throw Be5Exception.internalInQuery(query, e);
        }

        return getInitialized(abstractQueryExecutor, query, parameters);
    }

    private QueryExecutor getInitialized(AbstractQueryExecutor abstractQueryExecutor,
                                         Query query, Map<String, ?> parameters)
    {
        injector.injectMembers(abstractQueryExecutor);

        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) parameters;
        abstractQueryExecutor.initialize(query, params);

        return abstractQueryExecutor;
    }
}
