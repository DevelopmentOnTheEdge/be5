package com.developmentontheedge.be5.operation.test;

import com.developmentontheedge.be5.base.util.DpsUtils;
import com.developmentontheedge.be5.database.impl.SqlHelper;
import com.developmentontheedge.be5.operation.support.TestOperationSupport;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import javax.inject.Inject;
import java.util.Map;


public class TestOperation extends TestOperationSupport
{
    @Inject SqlHelper sqlHelper;

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
        sqlHelper.insert(getInfo().getEntityName(),
                DpsUtils.toLinkedHashMap((DynamicPropertySet) parameters));
    }

}
