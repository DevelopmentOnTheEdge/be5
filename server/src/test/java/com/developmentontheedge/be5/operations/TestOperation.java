package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.beans.DynamicProperty;
import java.util.Map;


public class TestOperation extends OperationSupport implements Operation
{

    @Override
    public Object getParameters(Map<String, String> presetValues) throws Exception
    {
        parameters.add(new DynamicProperty("name", "Name", String.class, "testName"));
        parameters.add(new DynamicProperty("number", "Number", Long.class, 1L));

        return parameters;
    }

    @Override
    public void invoke(Object parameters, OperationContext context) throws Exception
    {
        //String sql = generateSQL( connector, false );
        //db.insert(sql);
    }

}
