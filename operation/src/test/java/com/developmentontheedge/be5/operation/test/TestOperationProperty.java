package com.developmentontheedge.be5.operation.test;

import com.developmentontheedge.be5.database.impl.SqlHelper;
import com.developmentontheedge.be5.operation.support.TestOperationSupport;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import javax.inject.Inject;
import java.util.Map;


public class TestOperationProperty extends TestOperationSupport
{
    @Inject
    SqlHelper sqlHelper;

    @Override
    public Object getParameters(Map<String, Object> presetValues)
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport();
        dps.add(new DynamicProperty("getOrDefault", "Name", String.class,
                presetValues.getOrDefault("getOrDefault", "defaultValue")));

        dps.add(new DynamicProperty("getOrDefaultNumber", "Name", Long.class,
                presetValues.getOrDefault("getOrDefaultNumber", "3")));

        return dps;
    }

    @Override
    public void invoke(Object parameters)
    {
        DynamicPropertySet params = (DynamicPropertySet) parameters;
//        sqlHelper.insert(getInfo().getEntityName(),
//                DpsUtils.toLinkedHashMap((DynamicPropertySet) parameters));
    }

}
