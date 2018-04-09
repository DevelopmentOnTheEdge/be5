package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.operation.OperationResult;


public class SilentInsertOperation extends InsertOperation
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
