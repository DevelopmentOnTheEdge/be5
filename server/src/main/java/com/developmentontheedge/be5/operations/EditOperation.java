package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.google.common.collect.ObjectArrays;

import java.util.Map;

public class EditOperation extends OperationSupport implements Operation
{

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        Entity entity = getInfo().getEntity();

        dps = db.select("SELECT * FROM " + entity.getName() + " WHERE ID =?",
                rs -> sqlHelper.getDpsWithoutPrimaryKey(entity, rs), records[0]);

        sqlHelper.updateValuesWithSpecial(dps, presetValues);

        return dps;
    }

    @Override
    public void invoke(Object parameters, OperationContext context) throws Exception
    {
        db.update(sqlHelper.generateUpdateSql(getInfo().getEntity(), dps),
                ObjectArrays.concat(sqlHelper.getValues(dps), records[0]));
    }

}
