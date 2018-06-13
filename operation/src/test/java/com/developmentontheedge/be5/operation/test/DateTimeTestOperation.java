package com.developmentontheedge.be5.operation.test;

import com.developmentontheedge.be5.base.util.DpsUtils;
import com.developmentontheedge.be5.database.impl.SqlHelper;
import com.developmentontheedge.be5.operation.support.TestOperationSupport;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import javax.inject.Inject;
import java.sql.Date;
import java.util.Map;


public class DateTimeTestOperation extends TestOperationSupport
{
    @Inject SqlHelper sqlHelper;

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
        sqlHelper.insert(getInfo().getEntityName(),
                DpsUtils.toLinkedHashMap((DynamicPropertySet) parameters));
    }

}