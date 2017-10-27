package com.developmentontheedge.be5.operation;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Session;
import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.helpers.OperationHelper;
import com.developmentontheedge.be5.api.services.QRecService;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.components.FrontendConstants;
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.model.beans.DynamicPropertyGBuilder;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;


public abstract class OperationSupport extends DynamicPropertyGBuilder implements Operation
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

    public DynamicPropertySet dps = new DynamicPropertySetSupport();

    public static final String reloadControl = "_reloadcontrol_";

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
        HashUrl hashUrl = new HashUrl(FrontendConstants.FORM_ACTION, getInfo().getEntity().getName(),
                getInfo().getQueryName(), getInfo().getName());
        if(records.length > 0)
        {
            hashUrl = hashUrl.named("selectedRows", Arrays.stream(records).collect(Collectors.joining(",")));
        }

        setResult(OperationResult.redirect(hashUrl));
    }

    public void setResultRedirectThisOperationNewId(Object newID)
    {
        HashUrl hashUrl = new HashUrl(FrontendConstants.FORM_ACTION, getInfo().getEntity().getName(),
                getInfo().getQueryName(), getInfo().getName())
                .named("selectedRows", newID.toString());

        setResult(OperationResult.redirect(hashUrl));
    }

    public Object getLayout()
    {
        return Collections.emptyMap();
    }

}
