package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationSupport;

import java.util.Map;


public class SilentDeleteOperation extends OperationSupport implements Operation
{
    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        //todo add redirect params
        return null;
    }

    @Override
    public void invoke(Object parameters, OperationContext context) throws Exception
    {
        database.getEntity(getInfo().getEntityName()).remove(records);
    }
}
