package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.operation.OperationSupport;

import java.util.Map;


public class SilentDeleteOperation extends OperationSupport
{
    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        //todo add redirect params
        return null;
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        database.getEntity(getInfo().getEntityName()).remove(context.records);
    }
}
