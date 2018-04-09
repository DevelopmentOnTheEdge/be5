package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Map;


public class InsertOperation extends OperationSupport
{
    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport();
        dps = dpsHelper.addDpExcludeAutoIncrement(dps, getInfo().getModel(),
                                context.getOperationParams(), presetValues);

        return dpsHelper.setOperationParams(dps, context.getOperationParams());
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        database.getEntity(getInfo().getEntityName()).add((DynamicPropertySet)parameters);

        setResult(OperationResult.finished());
    }

}
