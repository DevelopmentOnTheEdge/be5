package com.developmentontheedge.be5.server.operations;

import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.server.operations.support.OperationSupport;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Map;

import static com.developmentontheedge.be5.databasemodel.util.DpsUtils.setValues;

public class CloneOperation extends OperationSupport
{
    protected Object lastInsertID;

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        DynamicPropertySet record = database.getEntity(getInfo().getEntityName()).get(context.getRecords()[0]);
        DynamicPropertySet dps = dpsHelper.addDpExcludeAutoIncrement(new DynamicPropertySetSupport(),
                getInfo().getModel(), context.getParams());
        setValues(dps, record);
        setValues(dps, presetValues);
        return dpsHelper.setOperationParams(dps, context.getParams());
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        DynamicPropertySet entityParams = dpsHelper.filterEntityParams(getInfo().getEntity(),
                (DynamicPropertySet) parameters);
        lastInsertID = database.getEntity(getInfo().getEntityName()).add(entityParams);

        setResult(OperationResult.finished());
    }
}
