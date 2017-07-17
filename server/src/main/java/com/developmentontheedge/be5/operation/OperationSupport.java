package com.developmentontheedge.be5.operation;

import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.SqlHelper;
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Map;
import java.util.stream.StreamSupport;

public abstract class OperationSupport implements Operation
{
    protected Injector injector;

    protected DatabaseService databaseService;
    protected DatabaseModel database;
    protected SqlService db;
    protected SqlHelper sqlHelper;
    protected Meta meta;

    private OperationInfo operationInfo;
    private OperationResult operationResult;

    public Long[] records;

    public DynamicPropertySet parameters = new DynamicPropertySetSupport();

    public static final String reloadControl = "_reloadcontrol_";

    @Override
    public final void initialize(Injector injector, OperationInfo operationInfo,
                                 OperationResult operationResult, Long[] records)
    {
        this.injector = injector;
        this.databaseService = injector.getDatabaseService();
        this.db = injector.getSqlService();
        this.database = injector.get(DatabaseModel.class);
        this.meta = injector.getMeta();
        this.sqlHelper = injector.get(SqlHelper.class);

        this.operationInfo = operationInfo;
        this.operationResult = operationResult;

        this.records = records;
    }

    @Override
    public final OperationInfo getInfo()
    {
        return operationInfo;
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

}
