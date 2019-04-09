package com.developmentontheedge.be5.modules.core.operations.system;

import com.developmentontheedge.be5.databasemodel.RecordModel;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.server.operations.EditOperation;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class SystemSettingsEdit extends EditOperation
{
    private Map<String, String> item;

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        String entityName = getInfo().getEntityName();
        String[] split = ((String) context.getRecord()).split("|");
        item = ImmutableMap.of(
                "section_name", split[0],
                "setting_name", split[1]
        );
        RecordModel<Object> record = database.getEntity(entityName).getBy(item);
        return getEditParameters(record, presetValues);
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        DynamicPropertySet entityParams = dpsHelper.filterEntityParams(getInfo().getEntity(),
                (DynamicPropertySet) parameters);

        //TODO in process
        //database.getEntity(getInfo().getEntityName()).set(context.getRecord(), entityParams);

        setResult(OperationResult.finished());
    }
}
