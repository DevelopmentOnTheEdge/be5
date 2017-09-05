package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.google.common.collect.ObjectArrays;

import java.util.Collection;
import java.util.Map;

public class SilentEditOperation extends OperationSupport implements Operation
{
    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        Entity entity = getInfo().getEntity();

        Collection<String> columns = dpsHelper.addUpdateSpecialColumns(entity, presetValues.keySet());

        dps = db.select("SELECT * FROM " + entity.getName() + " WHERE " + entity.getPrimaryKey() + " =?",
                rs -> dpsHelper.getDpsForColumns(entity, columns, rs), dpsHelper.castToTypePrimaryKey(entity, records[0]));

        dpsHelper.setValues(dps, presetValues);
        dpsHelper.updateSpecialColumns(dps);

        return dps;
    }

    @Override
    public void invoke(Object parameters, OperationContext context) throws Exception
    {
        Entity entity = getInfo().getEntity();

        db.update(dpsHelper.generateUpdateSqlForOneKey(entity, dps),
                ObjectArrays.concat(dpsHelper.getValues(dps), dpsHelper.castToTypePrimaryKey(entity, records[0])));
    }
}
