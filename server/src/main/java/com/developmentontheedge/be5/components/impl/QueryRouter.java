package com.developmentontheedge.be5.components.impl;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.Be5Caches;
import com.developmentontheedge.be5.api.services.GroovyRegister;
import com.developmentontheedge.be5.components.impl.model.TableModel;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.developmentontheedge.be5.components.impl.model.ActionHelper;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.TableBuilder;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class QueryRouter
{
    private static Cache<String, Class> groovyQueryClasses;

    //todo refactoring - remove static initialization, use be5MainSettings.getCacheSize() for fast test
    static {
        groovyQueryClasses = Caffeine.newBuilder()
                .maximumSize(1_000)
                .recordStats()
                .build();
        Be5Caches.registerCache("Groovy query classes", groovyQueryClasses);
    }

    public static interface Runner
    {
        void onStatic(Query query);

        void onTable(Query query, Map<String, String> parametersMap);

        void onTable(Query query, Map<String, String> parametersMap, TableModel tableModel);
        
        void onParametrizedTable(Query query, Map<String, String> parametersMap);
        
        void onError(String message);
    }
    
    public static QueryRouter on(Request req, Injector injector)
    {
        checkNotNull(req);
        return new QueryRouter(req, injector);
    }
    
    private final Request req;
    private final UserAwareMeta userAwareMeta;
    private final Meta meta;
    private final Injector injector;
    
    private QueryRouter(Request req, Injector injector)
    {
        this.injector = injector;
        this.req = req;
        this.userAwareMeta = injector.get(UserAwareMeta.class);
        this.meta = injector.get(Meta.class);
    }
    
    public void run(Runner runner)
    {
        checkNotNull(runner);
        
        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        Map<String, String> parametersMap = req.getStringValues(RestApiConstants.VALUES);
        
        routeAndRun(entityName, queryName, parametersMap, runner);
    }
    
    private void routeAndRun(String entityName, String queryName, Map<String, String> parametersMap, Runner runner) {
        routeAndRun(userAwareMeta.getQuery(entityName, queryName), parametersMap, runner);
    }
    
    private void routeAndRun(Query query, Map<String, String> parametersMap, Runner runner) {
        switch (query.getType())
        {
        case STATIC:
            if (ActionHelper.isStaticPage(query))
            {
                runner.onStatic(query);
                return;
            }
            runner.onError("Unsupported static request '" + query.getQuery() + "'.");
            return;
        case D1:
        // TODO check whether these cases are correct
        case D1_UNKNOWN:
        case D2:
        case CONTAINER:
        case CUSTOM:
        case JAVASCRIPT:
            if (meta.isParametrizedTable(query))
            {
                runner.onParametrizedTable(query, parametersMap);
            }
            else
            {
                runner.onTable(query, parametersMap);
            }
            return;
        case GROOVY:
            try
            {
                Class aClass = groovyQueryClasses.get(query.getEntity() + query.getName(),
                        k -> GroovyRegister.parseClass( query.getQuery() ));
                TableBuilder tableBuilder = (TableBuilder) aClass.newInstance();

                TableModel tableModel = tableBuilder
                        .initialize(query, parametersMap, req, injector)
                        .getTable();

                runner.onTable(query, parametersMap, tableModel);
            }
            catch( NoClassDefFoundError | IllegalAccessException | InstantiationException e )
            {
                throw Be5Exception.internal(e);
            }
            return;
        }
        
        throw new AssertionError("Unknown action type '" + query.getType() + "'");
    }
    
}
