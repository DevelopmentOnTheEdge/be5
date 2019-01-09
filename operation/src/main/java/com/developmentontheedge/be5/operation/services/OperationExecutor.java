package com.developmentontheedge.be5.operation.services;

import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationInfo;

import java.util.Map;


public interface OperationExecutor
{
    Object generate(Operation operation, Map<String, Object> presetValues);

    Object execute(Operation operation, Map<String, Object> presetValues);

    OperationContext getOperationContext(OperationInfo operationInfo,
                                         String queryName, Map<String, ?> operationParams);

    Operation create(OperationInfo operationInfo, String queryName,
                     Map<String, Object> operationParams);

    Operation create(OperationInfo operationInfo, OperationContext operationContext);
}
