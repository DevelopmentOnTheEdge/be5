package com.developmentontheedge.be5.components.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.Optional;

import com.developmentontheedge.be5.DatabaseConstants;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.developmentontheedge.be5.components.impl.model.Queries;
import com.developmentontheedge.be5.legacy.LegacyUrlParser;
import com.developmentontheedge.be5.legacy.LegacyUrlsService;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Query;

public class QueryRouter
{
    
    public static interface Runner
    {
        void onStatic(Query query);
        
        void onForm(String entityName, Optional<String> queryName, String operationName, Operation operation, Map<String, String> presetValues);
        
        void onTable(Query query, Map<String, String> parametersMap);
        
        void onParametrizedTable(Query query, Map<String, String> parametersMap);
        
        void onError(String message);
    }
    
    public static QueryRouter on(Request req, ServiceProvider serviceProvider)
    {
        checkNotNull(req);
        return new QueryRouter(req, serviceProvider);
    }
    
    private final Request req;
    private final UserAwareMeta userAwareMeta;
    private final LegacyUrlsService legacyQueriesService;
    private final Meta meta;
    
    private QueryRouter(Request req, ServiceProvider serviceProvider)
    {
        this.req = req;
        this.userAwareMeta = UserAwareMeta.get(req, serviceProvider);
        this.legacyQueriesService = serviceProvider.get(LegacyUrlsService.class);
        this.meta = serviceProvider.get(Meta.class);
    }
    
    public void run(Runner runner)
    {
        checkNotNull(runner);
        
        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        Map<String, String> parametersMap = req.getValues(RestApiConstants.VALUES);
        
        routeAndRun(entityName, queryName, parametersMap, runner);
    }
    
    private void routeAndRun(String entityName, String queryName, Map<String, String> parametersMap, Runner runner) {
        routeAndRun(userAwareMeta.getQuery(entityName, queryName), parametersMap, runner);
    }
    
    private void routeAndRun(Query query, Map<String, String> parametersMap, Runner runner) {
        switch (query.getType())
        {
        case Query.QUERY_TYPE_STATIC:
            if (Queries.isStaticPage(query))
            {
                runner.onStatic(query);
                return;
            }
            final LegacyUrlParser parser = legacyQueriesService.createParser(query.getQuery());
            if (parser.isLegacy())
            {
                if (!parser.isValid())
                {
                    runner.onError("Invalid legacy request '" + query.getQuery() + "'.");
                    return;
                }
                String targetEntityName = parser.getEntityName();
                String targetQueryName = parser.getQueryName();
                String targetOperationName = parser.getOperationName();
                
                if (targetOperationName != null)
                {
                    boolean useQueryName = targetQueryName != null;
                    Operation operation = userAwareMeta.getOperation(useQueryName, targetEntityName, targetQueryName, targetOperationName);
                    runner.onForm(targetEntityName, Optional.ofNullable(targetQueryName), targetOperationName, operation, parser.getParameters());
                    return;
                }
                else if (targetQueryName != null && targetOperationName == null)
                {
                    routeAndRun(targetEntityName, targetQueryName, parametersMap, runner); // XXX probably parameters are lost/replaced
                    return;
                }
                else if (targetEntityName != null)
                {
                    runner.onTable(userAwareMeta.getQuery(targetEntityName, DatabaseConstants.ALL_RECORDS_VIEW), parser.getParameters());
                    return;
                }
                
                runner.onError("Unsupported legacy request '" + query.getQuery() + "'.");
                return;
            }
            runner.onError("Unsupported static request '" + query.getQuery() + "'.");
            return;
        case Query.QUERY_TYPE_1D:
        // TODO check whether these cases are correct
        case Query.QUERY_TYPE_1DUNKNOWN:
        case Query.QUERY_TYPE_2D:
        case Query.QUERY_TYPE_CONTAINER:
        case Query.QUERY_TYPE_CUSTOM:
        case Query.QUERY_TYPE_JAVASCRIPT:
            if (meta.isParametrizedTable(query))
            {
                runner.onParametrizedTable(query, parametersMap);
            }
            else
            {
                runner.onTable(query, parametersMap);
            }
            return;
        }
        
        throw new AssertionError("Unknown action type '" + query.getType() + "'");
    }
    
}
