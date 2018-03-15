package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Map;


public class SilentEditOperation extends OperationSupport
{
    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        Entity entity = getInfo().getEntity();

        DynamicPropertySet dps = dpsHelper.addDpExcludeAutoIncrement(new DynamicPropertySetSupport(), getInfo().getModel());

        dpsHelper.setValues(dps, database.getEntity(entity.getName()).get(context.records[0]));

        dpsHelper.setOperationParams(dps, context.getOperationParams());

        return dpsHelper.setValues(dps, presetValues);
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        database.getEntity(getInfo().getEntityName()).set(context.records[0], (DynamicPropertySet)parameters);
    }
}
