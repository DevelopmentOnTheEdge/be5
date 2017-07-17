package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.beans.DynamicProperty;

import java.util.Map;


public class TestOperationProperty extends OperationSupport implements Operation
{

    @Override
    public Object getParameters(Map<String, String> presetValues) throws Exception
    {
        parameters.add(new DynamicProperty("simple", "Name", String.class));
        parameters.add(new DynamicProperty("simpleNumber", "Number", Long.class));

        parameters.add(new DynamicProperty("getOrDefault", "Name", String.class,
                presetValues.getOrDefault("getOrDefault", "defaultValue")));

        parameters.add(new DynamicProperty("getOrDefaultNumber", "Name", Long.class,
                presetValues.getOrDefault("getOrDefaultNumber", "3")));

        return parameters;
    }

    @Override
    public void invoke(Object parameters, OperationContext context) throws Exception
    {
        db.insert(sqlHelper.generateInsertSql(getInfo().getEntity(), this.parameters), sqlHelper.getValues(this.parameters));
    }

}
