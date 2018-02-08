package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.operation.OperationResult;


public class EditOperation extends SilentEditOperation
{
    @Override
    public void invoke(Object parameters) throws Exception
    {
        super.invoke(parameters);

        setResult(OperationResult.finished());
    }

}
