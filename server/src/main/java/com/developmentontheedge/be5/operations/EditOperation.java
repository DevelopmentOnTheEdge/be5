package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.sql.format.Ast;
import com.developmentontheedge.sql.model.AstDerivedColumn;
import com.developmentontheedge.sql.model.AstSelect;
import com.google.common.collect.ObjectArrays;

import java.util.Collections;
import java.util.Map;

public class EditOperation extends OperationSupport implements Operation
{

    @Override
    public Object getParameters(Map<String, String> presetValues) throws Exception
    {
        Entity entity = getInfo().getEntity();
        AstSelect sql = Ast.select(AstDerivedColumn.ALL)
                .from(entity.getName())
                .where(Collections.singletonMap(entity.getPrimaryKey(), "?"));

        DynamicPropertySet dps = db.select(sql.format(), rs -> sqlHelper.getDps(entity, rs), records[0]);

        for (Map.Entry<String, String> entry: presetValues.entrySet())
        {
            DynamicProperty property = dps.getProperty(entry.getKey());
            if( property!= null)
                property.setValue(entry.getValue());
        }

        return dps;
    }

    @Override
    public void invoke(Object parameters, OperationContext context) throws Exception
    {
        db.update(sqlHelper.generateUpdateSql(getInfo().getEntity(), dps), ObjectArrays.concat(sqlHelper.getValues(dps), records[0]));
    }

}
