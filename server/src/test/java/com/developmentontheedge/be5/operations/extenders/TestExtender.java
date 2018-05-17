package com.developmentontheedge.be5.operations.extenders;

import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operations.support.OperationExtenderSupport;


public class TestExtender extends OperationExtenderSupport
{
    @Override
    public void preInvoke(Operation op, Object parameters) throws Exception
    {
        db.update("update testTable name = 'preInvoke' WHERE 1=2");
    }

    @Override
    public void postInvoke(Operation op, Object parameters) throws Exception
    {
        db.update("update testTable name = 'postInvoke' WHERE 1=2");
    }
}
