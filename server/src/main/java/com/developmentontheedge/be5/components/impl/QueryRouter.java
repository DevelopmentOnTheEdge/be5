package com.developmentontheedge.be5.components.impl;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.services.GroovyRegister;
import com.developmentontheedge.be5.components.impl.model.TableModel;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.developmentontheedge.be5.components.impl.model.Queries;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.TableBuilder;

import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class QueryRouter
{
    
    public static interface Runner
    {
        void onStatic(Query query);
        
        void onForm(String entityName, Optional<String> queryName, String operationName, Operation operation, Map<String, String> presetValues);
        
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
        this.userAwareMeta = UserAwareMeta.get(injector);
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
            if (Queries.isStaticPage(query))
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
            String code = query.getQuery();
            try
            {
                TableBuilder tableBuilder = (TableBuilder)GroovyRegister.parseClass(code).newInstance();
                TableModel tableModel = tableBuilder.get(query, parametersMap, req, injector);
                runner.onTable(query, parametersMap, tableModel);
            }
            catch( NoClassDefFoundError | IllegalAccessException | InstantiationException e )
            {
                throw new UnsupportedOperationException( "Groovy feature has been excluded", e );
            }
            return;
        }
        
        throw new AssertionError("Unknown action type '" + query.getType() + "'");
    }
    
}
