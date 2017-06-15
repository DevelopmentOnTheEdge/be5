package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationSupport;
import java.util.Map;


public class InsertOperation extends OperationSupport implements Operation
{

    @Override
    public Object getParameters(Map<String, String> presetValues) throws Exception
    {
        dps = sqlHelper.getTableDps(getInfo().getEntity());
        sqlHelper.setValues(dps, presetValues);

        return dps;
    }

    @Override
    public void invoke(Object parameters, OperationContext context) throws Exception {
        db.insert(sqlHelper.generateInsertSql(dps, getInfo().getEntity()), sqlHelper.getValues(dps));
    }

}
