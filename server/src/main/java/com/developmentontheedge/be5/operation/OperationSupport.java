package com.developmentontheedge.be5.operation;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.helpers.OperationHelper;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.helpers.SqlHelper;
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Map;

public abstract class OperationSupport implements Operation
{
    public Injector injector;

    public DatabaseService databaseService;
    public DatabaseModel database;
    public SqlService db;
    public SqlHelper sqlHelper;
    public Meta meta;
    public OperationHelper helper;

    private OperationInfo operationInfo;
    private OperationResult operationResult;

    public String[] records;
    public Request request;

    public DynamicPropertySet dps = new DynamicPropertySetSupport();

    public static final String reloadControl = "_reloadcontrol_";

    @Override
    public final void initialize(Injector injector, OperationInfo operationInfo,
                                 OperationResult operationResult, String[] records, Request request)
    {
        this.injector = injector;
        this.databaseService = injector.getDatabaseService();
        this.db = injector.getSqlService();
        this.database = injector.get(DatabaseModel.class);
        this.meta = injector.getMeta();
        this.sqlHelper = injector.get(SqlHelper.class);
        this.helper = injector.get(OperationHelper.class);

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
}
