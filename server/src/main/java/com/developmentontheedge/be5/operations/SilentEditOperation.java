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

        Collection<String> columns = sqlHelper.addUpdateSpecialColumns(entity, presetValues.keySet());

        dps = db.select("SELECT * FROM " + entity.getName() + " WHERE " + entity.getPrimaryKey() + " =?",
                rs -> sqlHelper.getDpsForColumns(entity, columns, rs), sqlHelper.castToTypePrimaryKey(entity, records[0]));

        sqlHelper.setValues(dps, presetValues);
        sqlHelper.updateSpecialColumns(dps);

        return dps;
    }

    @Override
    public void invoke(Object parameters, OperationContext context) throws Exception
    {
        Entity entity = getInfo().getEntity();

        db.update(sqlHelper.generateUpdateSqlForOneKey(entity, dps),
                ObjectArrays.concat(sqlHelper.getValues(dps), sqlHelper.castToTypePrimaryKey(entity, records[0])));
    }
}
