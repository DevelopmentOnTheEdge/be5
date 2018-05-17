package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operations.support.OperationSupport;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Map;


public class EditOperation extends OperationSupport
{
    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        Entity entity = getInfo().getEntity();

        DynamicPropertySet dps = dpsHelper.addDpExcludeAutoIncrement(new DynamicPropertySetSupport(),
                getInfo().getModel(), context.getOperationParams());

        dpsHelper.setValues(dps, database.getEntity(entity.getName()).get(context.getRecord()));

        dpsHelper.setValues(dps, presetValues);

        return dpsHelper.setOperationParams(dps, context.getOperationParams());
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        database.getEntity(getInfo().getEntityName()).set(context.getRecord(), (DynamicPropertySet)parameters);

        setResult(OperationResult.finished());
    }
}
