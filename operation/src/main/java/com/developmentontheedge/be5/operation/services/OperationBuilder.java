package com.developmentontheedge.be5.operation.services;

import com.developmentontheedge.be5.base.meta.Meta;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.developmentontheedge.be5.operation.OperationConstants.SELECTED_ROWS;


public class OperationBuilder
{
    private OperationExecutor operationExecutor;
    private Meta meta;

    private Object[] records = new Object[]{};

    private String entityName;
    private String queryName;
    private String operationName;

    private Map<String, ?> values = Collections.emptyMap();
    private Map<String, Object> operationParams = Collections.emptyMap();

    @Inject
    OperationBuilder(Meta meta, OperationExecutor operationExecutor,
                            @Assisted("entityName") String entityName,
                            @Assisted("operationName") String operationName)
    {
        this.meta = meta;
        this.operationExecutor = operationExecutor;

        this.entityName = entityName;
        this.operationName = operationName;
    }

    public interface OperationsFactory
    {
        OperationBuilder create(@Assisted("entityName") String entityName,
                                @Assisted("operationName") String operationName);
    }

    public OperationBuilder setQueryName(String queryName)
    {
        this.queryName = queryName;
        return this;
    }

    public OperationBuilder setRecords(Object[] records)
    {
        this.records = records;
        return this;
    }

    public OperationBuilder setValues(Map<String, ?> values)
    {
        this.values = values;
        return this;
    }

    public OperationBuilder setOperationParams(Map<String, Object> operationParams)
    {
        this.operationParams = operationParams;
        return this;
    }

    public Object generate()
    {
        Operation operation = operationExecutor.create(getOperationInfo(), getOperationContext());
        operation.setResult(OperationResult.generate());

        return operationExecutor.generate(operation, (Map<String, Object>) values);
    }

    public Operation execute()
    {
        Operation operation = operationExecutor.create(getOperationInfo(), getOperationContext());
        operation.setResult(OperationResult.execute());

        operationExecutor.execute(operation, (Map<String, Object>) values);
        if (operation.getStatus() == OperationStatus.ERROR)
        {
            throw (RuntimeException) operation.getResult().getDetails();
        }

        return operation;
    }
//
//    @Override
//    public Object generate(@DelegatesTo(GOperationModelBaseBuilder.class) final Closure closure)
//    {
//        closure.setResolveStrategy( Closure.DELEGATE_FIRST );
//        closure.setDelegate( this );
//        closure.call();
//
//        return generate();
//    }
//
//    @Override
//    public Operation execute(@DelegatesTo(GOperationModelBaseBuilder.class) final Closure closure)
//    {
//        closure.setResolveStrategy( Closure.DELEGATE_FIRST );
//        closure.setDelegate( this );
//        closure.call();
//
//        return execute();
//    }

    private OperationInfo getOperationInfo()
    {
        com.developmentontheedge.be5.metadata.model.Operation operationModel =
                meta.getOperation(entityName, operationName);

        Objects.requireNonNull(operationModel, "Operation '" + entityName + "." + operationName + "' not found.");

        return new OperationInfo(operationModel);
    }

    private OperationContext getOperationContext()
    {
        operationParams = new HashMap<>(operationParams);
        if (records.length > 0)
        {
            operationParams.put(SELECTED_ROWS, Arrays.stream(records)
                    .map(Object::toString)
                    .collect(Collectors.joining(",")));
        }

        return operationExecutor.getOperationContext(getOperationInfo(), queryName, operationParams);
    }

    public Operation executeIfNotEmptyRecords(Object[] records)
    {
        if (records.length > 0)
        {
            setRecords(records);
            return execute();
        } else
        {
            return null;
        }
    }
}
