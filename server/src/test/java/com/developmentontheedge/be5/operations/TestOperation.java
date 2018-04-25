package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Map;


public class TestOperation extends OperationSupport
{
    @Override
    public Object getParameters(Map<String, Object> presetValues)
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport();
        dps.add(new DynamicProperty("name", "Name", String.class,
                presetValues.getOrDefault("name", "")));

        dps.add(new DynamicProperty("number", "Number", Long.class,
                presetValues.getOrDefault("number", "0")));

        return dps;
    }

    @Override
    public void invoke(Object parameters)
    {
        DynamicPropertySet dps = (DynamicPropertySet)parameters;
        db.insert(dpsHelper.generateInsertSql(getInfo().getEntity(), dps), dpsHelper.getValues(dps));

        redirectThisOperation();
    }

}
