package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationSupport;
import java.util.Map;

public class InsertOperation extends OperationSupport implements Operation
{

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        return sqlHelper.getDpsWithoutPrimaryKey(getInfo().getEntity());
    }

    @Override
    public void invoke(Object parameters, OperationContext context) throws Exception
    {
        db.insert(sqlHelper.generateInsertSql(getInfo().getEntity(), dps),
                  sqlHelper.getValues(dps));
    }

}
