package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationInfo;

import java.util.Map;


public interface OperationExecutor
{
    void callInvoke(Operation operation,
                    Object parameters, OperationContext operationContext);

    Operation create(OperationInfo operationInfo, String[] records);

    Object generate(Operation operation, Map<String, Object> presetValues);

    void execute(Operation operation, Map<String, Object> presetValues);
}
