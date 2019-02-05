package com.developmentontheedge.be5.operation.test;

import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.support.TestOperationSupport;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Map;


public class TestOperation extends TestOperationSupport
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
        DynamicPropertySet params = (DynamicPropertySet) parameters;
        db.insert("INSERT INTO "+getInfo().getEntityName()+" (name, value) VALUES (?, ?)",
                params.getValue("name"), params.getValue("value"));
        setResult(OperationResult.redirect(getBackUrl()));
    }

}
