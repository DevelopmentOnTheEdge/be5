package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationInfo;

import java.util.Map;

public interface OperationExecutor
{
    Operation create(OperationInfo operationInfo, String[] records, Request request);

    Object generate(Operation operation, Map<String, Object> presetValues);

    void execute(Operation operation, Map<String, Object> presetValues);
}
