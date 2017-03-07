package com.developmentontheedge.be5.components.impl;

import java.util.Map;
import java.util.Optional;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.components.impl.QueryRouter.Runner;
import com.developmentontheedge.be5.components.impl.model.TableModel;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Query;

public class TableCounter implements Runner
{
    
    public static void countAndSend(Request req, Response res, ServiceProvider serviceProvider) {
        QueryRouter.on(req, serviceProvider).run(new TableCounter(req, res, serviceProvider));
    }
    
    private final Request req;
    private final Response res;
    private final ServiceProvider serviceProvider;

    private TableCounter(Request req, Response res, ServiceProvider serviceProvider)
    {
        this.req = req;
        this.res = res;
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void onStatic(Query query)
    {
        res.sendError("It should be a table.");
    }

    @Override
    public void onForm(String entityName, Optional<String> queryName, String operationName, Operation operation, Map<String, String> presetValues)
    {
        res.sendError("It should be a table.");
    }

    @Override
    public void onTable(Query query, Map<String, String> parametersMap)
    {
        count(query, parametersMap);
    }
    
    @Override
    public void onParametrizedTable(Query query, Map<String, String> parametersMap)
    {
        count(query, parametersMap);
    }
    
    private void count(Query query, Map<String, String> parametersMap)
    {
        res.sendAsJson(TableModel.from(query, parametersMap, req, serviceProvider).count());
    }
    
    @Override
    public void onError(String message)
    {
        res.sendError(message);
    }

}
