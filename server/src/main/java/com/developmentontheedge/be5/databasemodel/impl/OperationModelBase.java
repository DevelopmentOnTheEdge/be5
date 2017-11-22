package com.developmentontheedge.be5.databasemodel.impl;

import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.databasemodel.OperationModel;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationStatus;

import java.util.Collections;
import java.util.Map;


class OperationModelBase implements OperationModel
{
    private OperationExecutor operationExecutor;
    private Meta meta;

    private String[] records = new String[]{ };

    private String entityName;
    private String queryName = null;
    private String operationName;

    private Map<String, Object> presetValues = Collections.emptyMap();

    OperationModelBase( Meta meta, OperationExecutor operationExecutor )
    {
        this.meta = meta;
        this.operationExecutor = operationExecutor;
    }

    @Override
    public OperationModel setEntityName(String entityName)
    {
        this.entityName = entityName;
        return this;
    }

    @Override
    public OperationModel setQueryName(String queryName)
    {
        this.queryName = queryName;
        return this;
    }

    @Override
    public OperationModel setOperationName(String operationName)
    {
        this.operationName = operationName;
        return this;
    }

    @Override
    public OperationModel setRecords( String... records )
    {
        this.records = records;
        return this;
    }

    @Override
    public OperationModel setPresetValues( Map<String, Object> presetValues )
    {
        this.presetValues = presetValues;
        return this;
    }

    @Override
    public Object getParameters() throws Exception
    {
        Operation operation = operationExecutor.create(getOperationInfo(), records);
        return operationExecutor.generate(operation, presetValues);
    }

    @Override
    public Operation execute()
    {
        Operation operation = operationExecutor.create(getOperationInfo(), records);

        operationExecutor.execute(operation, presetValues);
        if(operation.getStatus() == OperationStatus.ERROR)
        {
            throw (RuntimeException)operation.getResult().getDetails();
        }

        return operation;
    }

    private OperationInfo getOperationInfo()
    {
        com.developmentontheedge.be5.metadata.model.Operation operationModel =
                meta.getOperationIgnoringRoles(meta.getEntity(entityName), operationName);

        return new OperationInfo(queryName, operationModel);
    }
}