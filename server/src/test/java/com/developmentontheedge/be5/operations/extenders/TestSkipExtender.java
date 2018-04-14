package com.developmentontheedge.be5.operations.extenders;

import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationExtenderSupport;
import com.developmentontheedge.be5.operation.OperationResult;


public class TestSkipExtender extends OperationExtenderSupport
{
    @Override
    public void preInvoke(Operation op, Object parameters) throws Exception
    {
        db.update("update testTable name = 'preInvokeAfterSkip' WHERE 1=2");
    }

    @Override
    public boolean skipInvoke(Operation op, Object parameters)
    {
        op.setResult(OperationResult.finished("Skip invoke"));
        return true;
    }
}
