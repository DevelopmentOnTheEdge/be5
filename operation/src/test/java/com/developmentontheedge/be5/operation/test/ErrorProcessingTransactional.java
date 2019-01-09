package com.developmentontheedge.be5.operation.test;

import com.developmentontheedge.be5.operation.TransactionalOperation;
import com.developmentontheedge.be5.operation.support.TestOperationSupport;
import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertyBuilder;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Map;


public class ErrorProcessingTransactional extends TestOperationSupport implements TransactionalOperation
{
    private DynamicPropertySet dps = new DynamicPropertySetSupport();

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dps.add(new DynamicPropertyBuilder("name", String.class)
                .value(presetValues.getOrDefault("name", ""))
                .attr(BeanInfoConstants.COLUMN_SIZE_ATTR, 30)
                .get());

        DynamicProperty name = dps.getProperty("name");

        if (name.getValue().equals("generateError"))
        {
            throw new IllegalArgumentException();
        }

        return dps;
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {

    }
}
