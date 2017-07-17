package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.google.common.collect.ObjectArrays;

import java.util.Map;

public class EditOperation extends OperationSupport implements Operation
{

    @Override
    public Object getParameters(Map<String, String> presetValues) throws Exception
    {
        Entity entity = getInfo().getEntity();

        parameters = db.select("SELECT * FROM " + entity.getName() + " WHERE ID =?",
                rs -> sqlHelper.getDpsWithoutPrimaryKey(entity, rs), records[0]);

        for (Map.Entry<String, String> entry: presetValues.entrySet())
        {
            DynamicProperty property = parameters.getProperty(entry.getKey());
            if( property!= null)
                property.setValue(entry.getValue());
        }

        return parameters;
    }

    @Override
    public void invoke(Object parameters, OperationContext context) throws Exception
    {
        db.update(sqlHelper.generateUpdateSql(getInfo().getEntity(), this.parameters),
                ObjectArrays.concat(sqlHelper.getValues(this.parameters), records[0]));
    }

}
