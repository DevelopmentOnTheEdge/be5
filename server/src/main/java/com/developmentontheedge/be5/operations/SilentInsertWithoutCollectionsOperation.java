package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.log.Logger;

import java.util.Map;
import java.util.stream.Collectors;

public class SilentInsertWithoutCollectionsOperation extends OperationSupport
{
    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dps = sqlHelper.getDpsWithoutAutoIncrement(getInfo().getEntity(), presetValues);//todo test
        return dps;
    }

    @Override
    public void invoke(Object parameters, OperationContext context) throws Exception
    {
        checkDpsContainNotNullColumns(parameters);

        db.insert(sqlHelper.generateInsertSql(getInfo().getEntity(), dps),
                sqlHelper.getValues(dps));
    }

    private void checkDpsContainNotNullColumns(Object parameters){
        dps = (DynamicPropertySet)parameters;

        String errorMsg = meta.getColumns(getInfo().getEntity()).values().stream()
                .filter(column -> !column.isCanBeNull() && !column.isAutoIncrement() && column.getDefaultValue() == null
                        && !dps.hasProperty(column.getName()))
                .map(column -> "Dps not contain notNull column '" + column.getName()
                             + "' in entity '" + column.getEntity().getName()+ "'")
                .collect(Collectors.joining("\n"));

        if(!errorMsg.isEmpty())
        {
            throw Be5Exception.internal(JULLogger.infoBlock(errorMsg));
        }
    }
}
