package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationInfo;

import java.util.Map;


public interface OperationExecutor
{
    Object generate(Operation operation, Map<String, Object> presetValues);

    Object execute(Operation operation, Map<String, Object> presetValues);

    Operation create(com.developmentontheedge.be5.metadata.model.Operation operation, String queryName,
                     String[] selectedRows, Map<String, Object> operationParams);

    Operation create(OperationInfo operationInfo, OperationContext operationContext);
}