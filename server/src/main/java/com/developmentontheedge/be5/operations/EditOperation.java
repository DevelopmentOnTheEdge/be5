package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationSupport;
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

        dpsHelper.setValues(dps, database.getEntity(entity.getName()).get(context.records[0]));

        dpsHelper.setValues(dps, presetValues);

        return dpsHelper.setOperationParams(dps, context.getOperationParams());
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        Class<?> primaryKeyType = meta.getColumnType(getInfo().getEntity(), getInfo().getPrimaryKey());
        Object primaryKey;
        if(primaryKeyType == Long.class)
        {
            primaryKey = Long.parseLong(context.records[0]);
        }
        else
        {
            primaryKey = context.records[0];
        }

        database.getEntity(getInfo().getEntityName()).set(primaryKey, (DynamicPropertySet)parameters);

        setResult(OperationResult.finished());
    }
}
