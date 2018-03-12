package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationInfo;

import java.util.Map;


public interface OperationExecutor
{
    Object generate(Operation operation, Map<String, Object> presetValues);

    Object execute(Operation operation, Map<String, Object> presetValues);

    Operation create(String entityName, String queryName, String operationName);

    Operation create(String entityName, String queryName, String operationName,
                     String[] selectedRows, Map<String, String> operationParams);

    Operation create(OperationInfo operationInfo, OperationContext operationContext);
}
