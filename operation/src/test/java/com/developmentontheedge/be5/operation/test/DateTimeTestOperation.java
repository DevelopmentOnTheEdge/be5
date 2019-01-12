package com.developmentontheedge.be5.operation.test;

import com.developmentontheedge.be5.operation.support.TestOperationSupport;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.sql.Date;
import java.util.Map;


public class DateTimeTestOperation extends TestOperationSupport
{
    @Override
    public Object getParameters(Map<String, Object> presetValues)
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport();
        dps.add(new DynamicProperty("activeFrom", "activeFrom", Date.class,
                presetValues.getOrDefault("activeFrom", "1900-01-01")));

        return dps;
    }

    @Override
    public void invoke(Object parameters)
    {
        DynamicPropertySet params = (DynamicPropertySet) parameters;
        db.insert("INSERT INTO testtable (activeFrom) VALUES (?)", params.getValue("activeFrom"));
    }

}
