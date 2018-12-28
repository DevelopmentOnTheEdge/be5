package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.GroovyRegister;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryExecutor;
import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.be5.query.impl.Be5SqlQueryExecutor;
import com.developmentontheedge.be5.query.impl.CellFormatter;
import com.developmentontheedge.be5.query.impl.QueryMetaHelper;
import com.developmentontheedge.be5.query.impl.QuerySqlGenerator;
import com.developmentontheedge.be5.query.support.BaseQueryExecutorSupport;
import com.google.inject.Injector;

import javax.inject.Inject;
import java.util.Map;

import static com.developmentontheedge.be5.metadata.QueryType.D1;
import static com.developmentontheedge.be5.metadata.QueryType.D1_UNKNOWN;


public class QueryExecutorFactoryImpl implements QueryExecutorFactory
{
    private final Meta meta;
    private final DbService db;
    private final QuerySession querySession;
    private final UserInfoProvider userInfoProvider;
    private final QueryMetaHelper queryMetaHelper;
    private final CellFormatter cellFormatter;
    private final GroovyRegister groovyRegister;
    private final QuerySqlGenerator querySqlGenerator;
    private final Injector injector;

    @Inject
    public QueryExecutorFactoryImpl(Meta meta, DbService db, QuerySession querySession,
                                    UserInfoProvider userInfoProvider, QueryMetaHelper queryMetaHelper,
                                    CellFormatter cellFormatter, GroovyRegister groovyRegister,
                                    QuerySqlGenerator querySqlGenerator, Injector injector)
    {
        this.meta = meta;
        this.db = db;
        this.querySession = querySession;
        this.userInfoProvider = userInfoProvider;
        this.queryMetaHelper = queryMetaHelper;
        this.cellFormatter = cellFormatter;
        this.groovyRegister = groovyRegister;
        this.querySqlGenerator = querySqlGenerator;
        this.injector = injector;
    }

    @Override
    public QueryExecutor get(Query query, Map<String, ?> parameters)
    {
        if (query.getType() == QueryType.JAVA || query.getType() == QueryType.GROOVY)
        {
            return getQueryBuilder(query, parameters);
        }
        else if (query.getType() == D1 || query.getType() == D1_UNKNOWN)
        {
            return new Be5SqlQueryExecutor(query, parameters, querySession, userInfoProvider, meta, db, queryMetaHelper,
                    cellFormatter, querySqlGenerator);
        }
        else
        {
            throw Be5Exception.internal("Unknown action type '" + query.getType() + "'");
        }
    }

    private QueryExecutor getQueryBuilder(Query query, Map<String, ?> parameters)
    {
        BaseQueryExecutorSupport tableBuilder;

        switch (query.getType())
        {
            case JAVA:
                try
                {
                    tableBuilder = (BaseQueryExecutorSupport) Class.forName(query.getQuery()).newInstance();
                    break;
                }
                catch (ClassNotFoundException | IllegalAccessException | InstantiationException e)
                {
                    throw Be5Exception.internalInQuery(query, e);
                }
            case GROOVY:
                try
                {
                    Class aClass = groovyRegister.getClass(query.getEntity() + query.getName(),
                            query.getQuery(), query.getFileName());

                    if (aClass != null)
                    {
                        tableBuilder = (BaseQueryExecutorSupport) aClass.newInstance();
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
            default:
                throw Be5Exception.internal("Not support operation type: " + query.getType());
        }

        injector.injectMembers(tableBuilder);

        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) parameters;
        tableBuilder.initialize(query, params);

        return tableBuilder;
    }
}
