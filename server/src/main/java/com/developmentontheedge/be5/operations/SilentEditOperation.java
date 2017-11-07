package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.google.common.collect.ObjectArrays;

import java.util.Map;


public class SilentEditOperation extends OperationSupport implements Operation
{
    private DynamicPropertySet dps = new DynamicPropertySetSupport();

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        Entity entity = getInfo().getEntity();

        dps = dpsHelper.addDpExcludeAutoIncrement(new DynamicPropertySetSupport(), entity);

        dpsHelper.setValues(dps, database.getEntity(entity.getName()).get(records[0]));

        return dpsHelper.setValues(dps, presetValues);
    }

    @Override
    public void invoke(Object parameters, OperationContext context) throws Exception
    {
        Entity entity = getInfo().getEntity();

        dpsHelper.addInsertSpecialColumns(entity, dps);

        db.update(dpsHelper.generateUpdateSqlForOneKey(entity, dps),
                ObjectArrays.concat(dpsHelper.getValues(dps), dpsHelper.castToTypePrimaryKey(entity, records[0])));
    }
}
