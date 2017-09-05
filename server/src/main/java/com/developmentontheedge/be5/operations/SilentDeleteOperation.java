package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.google.common.collect.ObjectArrays;

import java.util.Map;

public class SilentDeleteOperation extends OperationSupport implements Operation
{
    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        return null;
    }

    @Override
    public void invoke(Object parameters, OperationContext context) throws Exception
    {
        db.update(dpsHelper.generateDeleteInSql(getInfo().getEntity(), context.getRecordIDs().length),
            ObjectArrays.concat(dpsHelper.getDeleteSpecialValues(getInfo().getEntity()),
                    dpsHelper.castToTypePrimaryKey(getInfo().getEntity(), context.getRecordIDs()), Object.class)
        );
    }
}
