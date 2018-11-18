package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.server.services.OperationLogging;

import java.util.Map;

public class OperationLoggingMock implements OperationLogging
{
    @Override
    public void saveOperationLog(Operation operation, Map<String, Object> values)
    {

    }
}
