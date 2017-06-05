package com.developmentontheedge.be5.operation;

import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Map;

public abstract class OperationSupport implements Operation
{
    public ServiceProvider sp;

    public DatabaseService databaseService;
    public SqlService db;
    private OperationContext operationContext;
    private OperationInfo meta;
    private OperationResult operationResult;
    public DynamicPropertySet parameters = new DynamicPropertySetSupport();

    @Override
    public final void initialize(ServiceProvider serviceProvider, OperationInfo meta,
                                 OperationResult operationResult)
    {
        this.sp = serviceProvider;
        this.meta = meta;
        this.operationResult = operationResult;

        db = sp.getSqlService();
        databaseService = sp.getDatabaseService();
    }

    @Override
    public final OperationInfo getInfo()
    {
        return meta;
    }

    @Override
    public Object getParameters(Map<String, String> presetValues) throws Exception
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

    @Override
    public OperationContext getContext()
    {
        return operationContext;
    }
}
