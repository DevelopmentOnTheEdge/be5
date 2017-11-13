package com.developmentontheedge.be5.operation;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Session;
import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.helpers.OperationHelper;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.QRecService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel;
import com.developmentontheedge.be5.env.Inject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public abstract class OperationSupport implements Operation
{
    @Inject public DatabaseService databaseService;
    @Inject public DatabaseModel database;
    @Inject public SqlService db;
    @Inject public QRecService qRec;
    @Inject public DpsHelper dpsHelper;
    @Inject public Meta meta;
    @Inject public OperationHelper helper;
    @Inject public Validator validator;

    private OperationInfo operationInfo;
    private OperationResult operationResult;

    public String[] records;

    public Request request;
    public Session session;

    public static final String reloadControl = "_reloadcontrol_";
    private final Map<String, Object> redirectParams = new HashMap<>();

    @Override
    public final void initialize(OperationInfo operationInfo,
                                 OperationResult operationResult, String[] records, Request request)
    {
        this.operationInfo = operationInfo;
        this.operationResult = operationResult;

        this.records = records;

        this.request = request;
        this.session = request.getSession();
    }

    @Override
    public final OperationInfo getInfo()
    {
        return operationInfo;
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
        setResult(getInfo().redirectThisOperation(records, getRedirectParams()));
    }

    public void setResultRedirectThisOperationNewId(Object newID)
    {
        setResult(getInfo().redirectThisOperationNewId(newID, getRedirectParams()));
    }

    public Object getLayout()
    {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> getRedirectParams()
    {
        return redirectParams;
    }

    /**
     * Puts additional parameters for redirect OperationResult.
     * @param extra parameters map
     */
    public void addRedirectParams( Map<String, Object> extra )
    {
        redirectParams.putAll( extra );
    }

    /**
     * Puts additional parameter for redirect OperationResult.
     * @param name  parameter name
     * @param value parameter value
     */
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

}
