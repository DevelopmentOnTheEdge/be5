package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationResult;


public class DeleteOperation extends SilentDeleteOperation implements Operation
{
    @Override
    public void invoke(Object parameters, OperationContext context) throws Exception
    {
        super.invoke(parameters, context);

        setResult(OperationResult.finished());
    }
}
