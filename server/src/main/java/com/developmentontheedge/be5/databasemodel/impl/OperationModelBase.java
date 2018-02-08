package com.developmentontheedge.be5.databasemodel.impl;

import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.databasemodel.OperationModel;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;


public class OperationModelBase implements OperationModel
{
    private OperationExecutor operationExecutor;
    private Meta meta;

    private String[] records = new String[]{ };

    private String entityName;
    private String queryName;
    private String operationName;

    private Map<String, ?> presetValues = Collections.emptyMap();

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
    public OperationModel setPresetValues( Map<String, ?> presetValues )
    {
        this.presetValues = presetValues;
        return this;
    }

    @Override
    public Object generate()
    {
        Operation operation = operationExecutor.create(getOperationInfo(), getOperationContext());
        operation.setResult(OperationResult.generate());

        return operationExecutor.generate(operation, (Map<String, Object>)presetValues);
    }

    @Override
    public Operation execute()
    {
        Operation operation = operationExecutor.create(getOperationInfo(), getOperationContext());
        operation.setResult(OperationResult.execute());

        operationExecutor.execute(operation, (Map<String, Object>) presetValues);
        if (operation.getStatus() == OperationStatus.ERROR)
        {
            throw (RuntimeException) operation.getResult().getDetails();
        }

        return operation;
    }

    @Override
    public Object generate(@DelegatesTo(GOperationModelBaseBuilder.class) final Closure closure)
    {
        closure.setResolveStrategy( Closure.DELEGATE_FIRST );
        closure.setDelegate( this );
        closure.call();

        return generate();
    }

    @Override
    public Operation execute(@DelegatesTo(GOperationModelBaseBuilder.class) final Closure closure)
    {
        closure.setResolveStrategy( Closure.DELEGATE_FIRST );
        closure.setDelegate( this );
        closure.call();

        return execute();
    }

    private OperationInfo getOperationInfo()
    {
        com.developmentontheedge.be5.metadata.model.Operation operationModel =
                meta.getOperationIgnoringRoles(entityName, operationName);

        Objects.requireNonNull(operationModel, "Operation '" + entityName + "." + operationName + "' not found.");

        return new OperationInfo(operationModel);
    }

    public OperationContext getOperationContext()
    {
        return new OperationContext(records, queryName, Collections.emptyMap());
    }

    public class GOperationModelBaseBuilder
    {
        public String[] records = new String[]{ };
        public String entityName;
        public String queryName;
        public String operationName;
        public Map<String, ?> presetValues = Collections.emptyMap();
    }
}