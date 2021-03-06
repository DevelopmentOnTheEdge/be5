package com.developmentontheedge.be5.server.operations;

import com.developmentontheedge.be5.operation.OperationResult;

public class SilentCloneOperation extends CloneOperation
{
    @Override
    public void invoke(Object parameters) throws Exception
    {
        super.invoke(parameters);

        if (isModalFormLayout())
        {
            setResult(OperationResult.finished(null, null, 0));
        }
        else
        {
            setResultGoBack();
        }
    }
}
