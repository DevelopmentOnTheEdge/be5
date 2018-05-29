package com.developmentontheedge.be5.server.operations;

import com.developmentontheedge.be5.server.operations.support.OperationSupport;
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

        dps.add(new DynamicProperty("value", "Value", Long.class,
                presetValues.getOrDefault("value", "0")));

        return dps;
    }

    @Override
    public void invoke(Object parameters)
    {
        database.getEntity(getInfo().getEntityName()).add((DynamicPropertySet)parameters);

        redirectThisOperation();
    }

}
