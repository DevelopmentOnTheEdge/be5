package com.developmentontheedge.be5.operation.extenders;

import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.operation.model.OperationResult;
import com.developmentontheedge.be5.operation.support.TestOperationExtenderSupport;


public class TestSkipExtender extends TestOperationExtenderSupport
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
