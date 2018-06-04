package com.developmentontheedge.be5.operation.support;

import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.operation.model.OperationContext;
import com.developmentontheedge.be5.operation.model.OperationInfo;
import com.developmentontheedge.be5.operation.model.OperationResult;
import com.developmentontheedge.be5.operation.model.OperationStatus;
import com.developmentontheedge.be5.operation.services.OperationsFactory;
import com.developmentontheedge.be5.operation.services.validation.Validator;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public abstract class BaseOperationSupport implements Operation
{
    @Inject public OperationsFactory operations;
    @Inject public DbService db;
    @Inject public Meta meta;
    @Inject public Validator validator;

    protected OperationInfo info;
    protected OperationContext context;
    private OperationResult operationResult;

    protected UserInfo userInfo;

    private final Map<String, Object> redirectParams = new HashMap<>();

    @Override
    public void initialize(OperationInfo info, OperationContext context, OperationResult operationResult)
    {
        this.info = info;
        this.context = context;
        this.operationResult = operationResult;
    }

    @Override
    public final OperationInfo getInfo()
    {
        return info;
    }

    @Override
    public OperationContext getContext()
    {
        return context;
    }

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        return null;
    }

    @Override
    public final void interrupt()
    {
        Thread.currentThread().interrupt();
    }

    @Override
    public final OperationStatus getStatus()
    {
        return operationResult.getStatus();
    }

    @Override
    public final OperationResult getResult()
    {
        return operationResult;
    }

    @Override
    public void setResult(OperationResult operationResult)
    {
        this.operationResult = operationResult;
    }

    public Query getQuery()
    {
        return meta.getQuery(getInfo().getEntityName(), context.getQueryName());
    }

    @Override
    public Map<String, Object> getRedirectParams()
    {
        Map<String, Object> map = new HashMap<>();

        for (Map.Entry<String, Object> entry : context.getOperationParams().entrySet())
        {
            if(!redirectParams.containsKey(entry.getKey()) && entry.getValue() != null)
            {
                map.put(entry.getKey(), entry.getValue().toString());
            }
        }

        for (Map.Entry<String, Object> entry : redirectParams.entrySet())
        {
            if(entry.getValue() != null && !entry.getValue().toString().isEmpty())
            {
                map.put(entry.getKey(), entry.getValue().toString());
            }
        }

        return map;
    }

    /**
     * Puts additional parameters for redirect OperationResult.
     * @param extra parameters map
     */
    @Override
    public void addRedirectParams( Map<String, ?> extra )
    {
        redirectParams.putAll( extra );
    }

    /**
     * Puts additional parameter for redirect OperationResult.
     * @param name  parameter name
     * @param value parameter value
     */
    @Override
    public void addRedirectParam( String name, Object value )
    {
        addRedirectParams( Collections.singletonMap( name, value ) );
    }

    @Override
    public void removeRedirectParam(String name)
    {
        redirectParams.put(name, "");
    }

//    todo addRedirectParam from DPS in invoke as be3
//    public void addNotNullRedirectParam( Map<String, Object> presetValues, String name )
//    {
//        Object value = presetValues.get(name);
//        if(value != null)
//        {
//            addRedirectParams(Collections.singletonMap(name, value.toString()));
//        }
//    }
//
//    public void addNotNullRedirectParam( Map<String, Object> presetValues )
//    {
//        presetValues.forEach((key, value) -> addNotNullRedirectParam(presetValues, key));
//    }

}
