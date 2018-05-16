package com.developmentontheedge.be5.operation.support;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.RestApiConstants;
import com.developmentontheedge.be5.api.Session;
import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.helpers.OperationHelper;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.api.FrontendConstants;
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel;
import javax.inject.Inject;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.UserInfo;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.be5.util.HashUrlUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public abstract class OperationSupport implements Operation
{
    @Inject public DatabaseModel database;
    @Inject public SqlService db;
    @Inject public DpsHelper dpsHelper;
    @Inject public Meta meta;
    @Inject public OperationHelper helper;
    @Inject public Validator validator;

    protected OperationInfo info;
    protected OperationContext context;
    private OperationResult operationResult;

    protected Request request;
    protected Session session;
    protected UserInfo userInfo;

    private final Map<String, Object> redirectParams = new HashMap<>();

    @Override
    public final void initialize(OperationInfo info, OperationContext context, OperationResult operationResult)
    {
        this.info = info;
        this.context = context;
        this.operationResult = operationResult;

        this.request = UserInfoHolder.getRequest();
        this.session = UserInfoHolder.getSession();
        this.userInfo = UserInfoHolder.getUserInfo();
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

    public void redirectThisOperation()
    {
        setResult(OperationResult.redirect(HashUrlUtils.getUrl(this).toString()));
    }

    public void redirectThisOperationNewId(Object newID)
    {
        setResult(OperationResult.redirect(getUrlForNewRecordId(newID).toString()));
    }

    public void redirectToTable(String entityName, String queryName, Map<String, Object> params)
    {
        setResult(OperationResult.redirect(new HashUrl(FrontendConstants.TABLE_ACTION, entityName, queryName).named(params).toString()));
    }

    public void redirectToTable(Query query, Map<String, Object> params)
    {
        setResult(OperationResult.redirect(new HashUrl(FrontendConstants.TABLE_ACTION, query.getEntity().getName(), query.getName()).named(params).toString()));
    }

    public Query getQuery()
    {
        return meta.getQuery(getInfo().getEntityName(), context.getQueryName());
    }

    public HashUrl getUrlForNewRecordId(Object newID)
    {
        return new HashUrl(FrontendConstants.FORM_ACTION, getInfo().getEntityName(), context.getQueryName(), getInfo().getName())
                .named(getRedirectParams())
                .named(RestApiConstants.SELECTED_ROWS, newID.toString());
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

    public <T> T getValueOrDefault(T value, T defaultValue)
    {
        return value == null ? defaultValue : value;
    }
}
