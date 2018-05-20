package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.operations.support.OperationSupport;
import com.developmentontheedge.be5.util.DpsUtils;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Map;


public class TestOperationProperty extends OperationSupport
{

    @Override
    public Object getParameters(Map<String, Object> presetValues)
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport();
        dps.add(new DynamicProperty("simple", "Name", String.class));
        dps.add(new DynamicProperty("simpleNumber", "Number", Long.class));

        dps.add(new DynamicProperty("getOrDefault", "Name", String.class,
                presetValues.getOrDefault("getOrDefault", "defaultValue")));

        dps.add(new DynamicProperty("getOrDefaultNumber", "Name", Long.class,
                presetValues.getOrDefault("getOrDefaultNumber", "3")));

        return DpsUtils.setValues(dps, presetValues);
    }

    @Override
    public void invoke(Object parameters)
    {
        database.getEntity(getInfo().getEntityName()).add((DynamicPropertySet)parameters);
    }

}
