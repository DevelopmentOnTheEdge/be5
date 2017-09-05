package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.beans.DynamicProperty;

import java.util.Map;


public class TestOperationProperty extends OperationSupport implements Operation
{

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dps.add(new DynamicProperty("simple", "Name", String.class));
        dps.add(new DynamicProperty("simpleNumber", "Number", Long.class));

        dps.add(new DynamicProperty("getOrDefault", "Name", String.class,
                presetValues.getOrDefault("getOrDefault", "defaultValue")));

        dps.add(new DynamicProperty("getOrDefaultNumber", "Name", Long.class,
                presetValues.getOrDefault("getOrDefaultNumber", "3")));

        return dps;
    }

    @Override
    public void invoke(Object parameters, OperationContext context) throws Exception
    {
        db.insert(dpsHelper.generateInsertSql(getInfo().getEntity(), dps), dpsHelper.getValues(dps));
    }

}
