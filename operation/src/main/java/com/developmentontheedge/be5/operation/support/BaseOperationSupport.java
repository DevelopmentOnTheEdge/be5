package com.developmentontheedge.be5.operation.support;

import com.developmentontheedge.be5.FrontendConstants;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationConstants;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.util.HashUrl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.developmentontheedge.be5.FrontendConstants.TABLE_ACTION;


public abstract class BaseOperationSupport implements Operation
{
    protected OperationInfo info;
    protected OperationContext context;
    private OperationResult operationResult;

    private final Map<String, Object> redirectParams = new LinkedHashMap<>();

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

    @Override
    public Map<String, Object> getRedirectParams()
    {
        Map<String, Object> map = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : context.getParams().entrySet())
        {
            if (!redirectParams.containsKey(entry.getKey()) && entry.getValue() != null)
            {
                map.put(entry.getKey(), getValue(entry));
            }
        }

        for (Map.Entry<String, Object> entry : redirectParams.entrySet())
        {
            if (entry.getValue() != null && !entry.getValue().toString().isEmpty())
            {
                map.put(entry.getKey(), getValue(entry));
            }
        }

        return map;
    }

    private Object getValue(Map.Entry<String, Object> entry)
    {
        if (entry.getValue() instanceof String[])
        {
            return String.join(",", (String[]) entry.getValue());
        }
        else
        {
            return entry.getValue().toString();
        }
    }

    /**
     * Puts additional parameters for redirect OperationResult.
     *
     * @param extra parameters map
     */
    @Override
    public void addRedirectParams(Map<String, ?> extra)
    {
        redirectParams.putAll(extra);
    }

    /**
     * Puts additional parameter for redirect OperationResult.
     *
     * @param name  parameter name
     * @param value parameter value
     */
    @Override
    public void addRedirectParam(String name, Object value)
    {
        addRedirectParams(Collections.singletonMap(name, value));
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

    public void redirectThisOperationNewId(Object newID)
    {
        setResult(OperationResult.redirect(getUrlForNewRecordId(newID).toString()));
    }

    public void redirectToTable(String entityName, String queryName, Map<String, Object> params)
    {
        setResult(OperationResult.redirect(getTableUrl(entityName, queryName, params)));
    }

    public void redirectToTable(String entityName, String queryName)
    {
        redirectToTable(entityName, queryName, getRedirectParams());
    }

    public void redirectToTable(Query query, Map<String, Object> params)
    {
        redirectToTable(query.getEntity().getName(), query.getName(), params);
    }

    public void redirectToTable()
    {
        redirectToTable(info.getEntityName(), context.getQueryName(), getRedirectParams());
    }

    @Override
    public void setResultGoBack()
    {
        setResult(OperationResult.redirect(new HashUrl(TABLE_ACTION,
                getInfo().getEntityName(), getContext().getQueryName())
                .named(getRedirectParams()).toString()));
    }

    public String getBackUrl()
    {
        return getTableUrl(info.getEntityName(), context.getQueryName(), context.getParams());
    }

    private String getTableUrl(String entityName, String queryName, Map<String, Object> params)
    {
        return new HashUrl(FrontendConstants.TABLE_ACTION, entityName, queryName)
                .named(params).toString();
    }

    public HashUrl getUrlForNewRecordId(Object newID)
    {
        Map<String, Object> params = getRedirectParams();
        params.put(OperationConstants.SELECTED_ROWS, newID.toString());
        return new HashUrl(FrontendConstants.FORM_ACTION, info.getEntityName(), context.getQueryName(), info.getName())
                .named(params);
    }

}
