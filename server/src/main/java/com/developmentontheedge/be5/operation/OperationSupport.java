package com.developmentontheedge.be5.operation;

import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Map;
import java.util.stream.StreamSupport;

public abstract class OperationSupport implements Operation
{
    public Injector injector;

    public DatabaseService databaseService;
    public SqlService db;
    public Meta meta;
    private OperationContext operationContext;
    private OperationInfo operationInfo;
    private OperationResult operationResult;
    public DynamicPropertySet dps = new DynamicPropertySetSupport();

    @Override
    public final void initialize(Injector injector, OperationInfo operationInfo,
                                 OperationResult operationResult)
    {
        this.injector = injector;
        this.operationInfo = operationInfo;
        this.meta = injector.getMeta();
        this.operationResult = operationResult;

        db = this.injector.getSqlService();
        databaseService = this.injector.getDatabaseService();
    }

    @Override
    public final OperationInfo getInfo()
    {
        return operationInfo;
    }

    @Override
    public Object getParameters(Map<String, String> presetValues) throws Exception
    {
        return null;
    }

    @Override
    public final void interrupt()
    {
        Thread.currentThread().interrupt();
    }

    @Override
    public final OperationStatus getStatus()
    {
        return operationResult.getStatus();
    }

    @Override
    public final OperationResult getResult()
    {
        return operationResult;
    }

    @Override
    public void setResult(OperationResult operationResult)
    {
        this.operationResult = operationResult;
    }

    @Override
    public OperationContext getContext()
    {
        return operationContext;
    }

    protected DynamicPropertySet getTableBean(String entityName) throws Exception
    {
        Entity entity = meta.getEntity(entityName);
        Map<String, ColumnDef> columns = meta.getColumns(entity);

        DynamicPropertySet dps = new DynamicPropertySetSupport();

        for (Map.Entry<String, ColumnDef> entry: columns.entrySet())
        {
            ColumnDef columnDef = entry.getValue();
            if(!columnDef.getName().equals(entity.getPrimaryKey()))
            {
                dps.add(getDynamicProperty(columnDef));
            }
        }
        return dps;
    }

    private DynamicProperty getDynamicProperty(ColumnDef columnDef)
    {
        return new DynamicProperty(columnDef.getName(), meta.getColumnType(columnDef));
    }

    protected void setValues(DynamicPropertySet dps, Map<String, String> presetValues)
    {
        StreamSupport.stream(dps.spliterator(), false).forEach(
                property -> {
                    property.setValue(presetValues.getOrDefault(property.getName(), getDefault(property.getType())));
                }
        );
    }

    protected String getDefault(Class<?> type){
        if(type == Long.class ||type == Integer.class ||type == Double.class ||type == Float.class){
            return "0";
        }
        return "";
    }

    protected Object[] getValues(DynamicPropertySet dps)
    {
        return StreamSupport.stream(dps.spliterator(), false)
                .map(DynamicProperty::getValue).toArray();
    }

}
