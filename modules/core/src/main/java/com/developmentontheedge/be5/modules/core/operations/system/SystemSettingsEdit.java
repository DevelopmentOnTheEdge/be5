package com.developmentontheedge.be5.modules.core.operations.system;

import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.server.operations.EditOperation;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class SystemSettingsEdit extends EditOperation
{
    private Map<String, String> conditions;

    protected DynamicPropertySet getRecordData(Map<String, Object> presetValues)
    {
        String entityName = getInfo().getEntityName();
        String[] split = ((String) context.getRecord()).split("\\|");
        conditions = ImmutableMap.of(
                "section_name", split[0],
                "setting_name", split[1]
        );
        return database.getEntity(entityName).getPropertySet(conditions);
    }

    @Override
    public void invoke(Object parameters)
    {
        DynamicPropertySet values = dpsHelper.filterEntityParams(getInfo().getEntity(),
                (DynamicPropertySet) parameters);
        database.getEntity(getInfo().getEntityName()).setBy(values, conditions);
        setResult(OperationResult.finished());
    }
}
