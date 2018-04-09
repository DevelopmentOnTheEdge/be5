package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.operation.OperationResult;


public class SilentDeleteOperation extends DeleteOperation
{
    @Override
    public void invoke(Object parameters) throws Exception
    {
        super.invoke(parameters);

        setResult(OperationResult.redirectToTable(
                getInfo().getEntityName(),
                getContext().getQueryName(),
                getRedirectParams()
        ));
    }
}
