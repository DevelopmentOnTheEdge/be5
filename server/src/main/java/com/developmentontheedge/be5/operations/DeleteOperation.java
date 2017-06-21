package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationSupport;

import java.util.Map;


public class DeleteOperation extends OperationSupport implements Operation
{

    @Override
    public Object getParameters(Map<String, String> presetValues) throws Exception
    {
        return null;
    }

    @Override
    public void invoke(Object parameters, OperationContext context) throws Exception
    {
        db.update(sqlHelper.generateDeleteInSql(getInfo().getEntity(), context.getRecordIDs().length),
                context.getRecordIDs());
    }

}
