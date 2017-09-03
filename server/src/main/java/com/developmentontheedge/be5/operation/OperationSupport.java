package com.developmentontheedge.be5.operation;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.helpers.OperationHelper;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.helpers.SqlHelper;
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Map;

public abstract class OperationSupport implements Operation
{
    @Inject public DatabaseService databaseService;
    @Inject public DatabaseModel database;
    @Inject public SqlService db;
    @Inject public SqlHelper sqlHelper;
    @Inject public Meta meta;
    @Inject public OperationHelper helper;
    @Inject public Validator validator;

    private OperationInfo operationInfo;
    private OperationResult operationResult;

    public String[] records;
    public Request request;

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

    public Object getLayout()
    {
        return null;
    }

    public final String RELOAD_ON_CHANGE = BeanInfoConstants.RELOAD_ON_CHANGE;
    public final String MULTIPLE_SELECTION_LIST = BeanInfoConstants.MULTIPLE_SELECTION_LIST;

    public final String VALIDATION_RULES = BeanInfoConstants.VALIDATION_RULES;
}
