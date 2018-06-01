package com.developmentontheedge.be5.server.operations.extenders;

import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.server.operations.support.OperationExtenderSupport;
import com.developmentontheedge.be5.operation.model.OperationResult;


public class TestSkipExtender extends OperationExtenderSupport
{
    @Override
    public void preInvoke(Operation op, Object parameters) throws Exception
    {
        db.update("update testTable name = 'preInvokeBeforeSkip' WHERE 1=2");
    }

    @Override
    public boolean skipInvoke(Operation op, Object parameters)
    {
        op.setResult(OperationResult.finished("Skip invoke"));
        return true;
    }
}