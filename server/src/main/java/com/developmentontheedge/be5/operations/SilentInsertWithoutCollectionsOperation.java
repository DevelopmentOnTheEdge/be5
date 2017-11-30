package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Map;


public class SilentInsertWithoutCollectionsOperation extends OperationSupport
{
    private DynamicPropertySet dps = new DynamicPropertySetSupport();

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        return dpsHelper.addDpExcludeAutoIncrement(dps, getInfo().getEntity(), presetValues);
    }

    @Override
    public void invoke(Object parameters, OperationContext context) throws Exception
    {
        dps = (DynamicPropertySet)parameters;

        Entity entity = getInfo().getEntity();

        dpsHelper.addInsertSpecialColumns(entity, dps);
        dpsHelper.checkDpsColumns(entity, dps);

        db.insert(dpsHelper.generateInsertSql(entity, dps), dpsHelper.getValues(dps));
    }

}
