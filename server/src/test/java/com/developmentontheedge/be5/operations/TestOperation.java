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
        parameters.add(new DynamicProperty("name", "Name", String.class,
                presetValues.getOrDefault("name", "")));

        parameters.add(new DynamicProperty("number", "Number", Long.class,
                presetValues.getOrDefault("number", "0")));

        return parameters;
    }

    @Override
    public void invoke(Object parameters, OperationContext context) throws Exception
    {
        db.insert(sqlHelper.generateInsertSql(getInfo().getEntity(), this.parameters), sqlHelper.getValues(this.parameters));
    }

}
