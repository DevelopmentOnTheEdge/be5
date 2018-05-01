package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.be5.util.Utils;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Map;


public class EditOperation extends OperationSupport
{
    private Class<?> primaryKeyType;

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        primaryKeyType = meta.getColumnType(getInfo().getEntity(), getInfo().getPrimaryKey());

        Entity entity = getInfo().getEntity();

        DynamicPropertySet dps = dpsHelper.addDpExcludeAutoIncrement(new DynamicPropertySetSupport(),
                getInfo().getModel(), context.getOperationParams());

        dpsHelper.setValues(dps, database.getEntity(entity.getName()).get(Utils.changeType(context.records[0], primaryKeyType)));

        dpsHelper.setValues(dps, presetValues);

        return dpsHelper.setOperationParams(dps, context.getOperationParams());
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        Object primaryKey = Utils.changeType(context.records[0], primaryKeyType);

        database.getEntity(getInfo().getEntityName()).set(primaryKey, (DynamicPropertySet)parameters);

        setResult(OperationResult.finished());
    }
}
