package com.developmentontheedge.be5.operation;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Session;
import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.helpers.OperationHelper;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.QRecService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.api.FrontendConstants;
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.UserInfo;
import com.developmentontheedge.be5.util.HashUrl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


public abstract class OperationSupport implements Operation
{
    @Inject public DatabaseModel database;
    @Inject public SqlService db;
    @Inject public QRecService qRec;
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

    public void setResultRedirectThisOperation()
    {
        setResult(OperationResult.redirect(getUrl()));
    }

    public void setResultRedirectThisOperationNewId(Object newID)
    {
        setResult(OperationResult.redirect(getUrlForNewRecordId(newID)));
    }

    public Query getQuery()
    {
        return meta.getQuery(getInfo().getEntityName(), context.getQueryName(), userInfo.getCurrentRoles());
    }

    @Override
    public HashUrl getUrl()
    {
        HashUrl hashUrl = new HashUrl(FrontendConstants.FORM_ACTION, getInfo().getEntityName(), context.getQueryName(), getInfo().getName())
                .named(getRedirectParams());
        if(context.getRecords().length > 0)
        {
            hashUrl = hashUrl.named("selectedRows", Arrays.stream(context.getRecords()).collect(Collectors.joining(",")));
        }

        return hashUrl;
    }

    public HashUrl getUrlForNewRecordId(Object newID)
    {
        return new HashUrl(FrontendConstants.FORM_ACTION, getInfo().getEntityName(), context.getQueryName(), getInfo().getName())
                .named(getRedirectParams())
                .named("selectedRows", newID.toString());
    }

    @Override
    public Map<String, String> getRedirectParams()
    {
        HashMap<String, String> stringStringHashMap = new HashMap<>();

        for (Map.Entry<String, String> entry : context.getOperationParams().entrySet())
        {
            if(entry.getValue() != null)stringStringHashMap.put(entry.getKey(), entry.getValue().toString());
        }

        for (Map.Entry<String, Object> entry : redirectParams.entrySet())
        {
            if(entry.getValue() != null)
            {
                if(!entry.getValue().toString().isEmpty())
                {
                    stringStringHashMap.put(entry.getKey(), entry.getValue().toString());
                }
                else
                {
                    stringStringHashMap.remove(entry.getKey());
                }
            }
        }
        return stringStringHashMap;
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
