package com.developmentontheedge.be5.server.operations;

import com.developmentontheedge.be5.databasemodel.RecordModel;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.server.operations.support.OperationSupport;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Map;

import static com.developmentontheedge.be5.databasemodel.util.DpsUtils.setValues;


public class EditOperation extends OperationSupport
{
    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        String entityName = getInfo().getEntityName();
        RecordModel<Object> record = database.getEntity(entityName).get(context.getRecord());
        return getEditParameters(record, presetValues);
    }

    protected Object getEditParameters(DynamicPropertySet record, Map<String, Object> presetValues)
    {
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
        database.getEntity(getInfo().getEntityName()).set(context.getRecord(), entityParams);

        setResult(OperationResult.finished());
    }
}
