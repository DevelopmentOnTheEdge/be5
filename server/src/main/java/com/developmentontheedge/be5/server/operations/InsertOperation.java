package com.developmentontheedge.be5.server.operations;

import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.server.operations.support.OperationSupport;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Map;


public class InsertOperation extends OperationSupport
{
    protected Object lastInsertID;

    public Object getLastInsertID()
    {
        return lastInsertID;
    }

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        return dpsHelper.addDpExcludeAutoIncrement(new DynamicPropertySetSupport(), getInfo().getModel(),
                context.getParams(), presetValues);
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        DynamicPropertySet entityParams = dpsHelper.filterEntityParams(getInfo().getEntity(),
                (DynamicPropertySet) parameters);
        lastInsertID = database.getEntity(getInfo().getEntityName()).add(entityParams);

        setResult(OperationResult.finished());
    }
}
