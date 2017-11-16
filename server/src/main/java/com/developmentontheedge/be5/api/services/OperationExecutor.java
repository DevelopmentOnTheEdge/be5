package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationInfo;

import java.util.Map;

public interface OperationExecutor
{
    Operation create(OperationInfo operationInfo, String[] records, Request request);

    Object generate(OperationInfo meta, Map<String, Object> presetValues, String[] selectedRows, Request req);

    void execute(OperationInfo meta, Map<String, Object> presetValues, String[] selectedRows, Request req);
}
