package com.developmentontheedge.be5.operation.extenders;

import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.operation.support.TestOperationExtenderSupport;


public class TestExtender extends TestOperationExtenderSupport
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
