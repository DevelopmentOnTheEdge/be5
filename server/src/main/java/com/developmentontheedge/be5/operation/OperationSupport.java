package com.developmentontheedge.be5.operation;

import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.SqlColumnType;
import com.developmentontheedge.be5.metadata.model.TableDef;
import com.developmentontheedge.be5.metadata.model.base.BeCaseInsensitiveCollection;
import com.developmentontheedge.be5.metadata.model.base.BeModelElement;
import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.sql.Date;
import java.sql.Time;
import java.util.Map;
import java.util.stream.StreamSupport;

public abstract class OperationSupport implements Operation
{
    public Injector injector;

    public DatabaseService databaseService;
    public SqlService db;
    private OperationContext operationContext;
    private OperationInfo meta;
    private OperationResult operationResult;
    public DynamicPropertySet dps = new DynamicPropertySetSupport();

    @Override
    public final void initialize(Injector injector, OperationInfo meta,
                                 OperationResult operationResult)
    {
        this.injector = injector;
        this.meta = meta;
        this.operationResult = operationResult;

        db = this.injector.getSqlService();
        databaseService = this.injector.getDatabaseService();
    }

    @Override
    public final OperationInfo getInfo()
    {
        return meta;
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

    public DynamicPropertySet getTableBean(String entityName) throws Exception
    {
        Project project = injector.getProject();
        Entity entity = project.getEntity(entityName);

        BeModelElement scheme = entity.getAvailableElement("Scheme");

        if(scheme == null)return null;

        BeModelElement columns = ((TableDef) scheme).get("Columns");

        DynamicPropertySet bean = new DynamicPropertySetSupport();


        ((BeCaseInsensitiveCollection<ColumnDef>) columns).stream()
                .filter(x -> !x.getName().equals(entity.getPrimaryKey()))
                .map(OperationSupport::getDynamicProperty)
                .forEach(bean::add);

        return bean;
    }

    static DynamicProperty getDynamicProperty(ColumnDef columnDef)
    {
        DynamicProperty dynamicProperty = new DynamicProperty(columnDef.getName(), getTypeClass(columnDef.getType()));
        dynamicProperty.setAttribute(BeanInfoConstants.COLUMN_SIZE_ATTR, columnDef.getType().getSize());

        return dynamicProperty;
    }

    private static Class<?> getTypeClass(SqlColumnType columnType)
    {
        switch( columnType.getTypeName() )
        {
            case SqlColumnType.TYPE_BIGINT:
                return Long.class;
            case SqlColumnType.TYPE_INT:
                return Integer.class;
            case SqlColumnType.TYPE_DECIMAL:
                return Double.class;
            case SqlColumnType.TYPE_BOOL:
                return Boolean.class;
            case SqlColumnType.TYPE_DATE:
                return Date.class;
            case SqlColumnType.TYPE_TIMESTAMP:
                return Time.class;
            default:
                return String.class;
        }
    }

    public void setValues(DynamicPropertySet dps, Map<String, String> presetValues)
    {
        presetValues.forEach((key,value) -> dps.getProperty(key).setValue(getValue(dps.getProperty(key).getType(), value)));
    }

    protected Object[] getValues(DynamicPropertySet dps)
    {
        return StreamSupport.stream(dps.spliterator(), false)
                .map(DynamicProperty::getValue).toArray();
    }

    protected Object getValue(Class<?> type, String value){
        if(type.isAssignableFrom(Integer.class)){
            return Integer.parseInt(value);
        }
        if(type.isAssignableFrom(Long.class)){
            return Long.parseLong(value);
        }
        if(type.isAssignableFrom(Double.class)){
            return Double.parseDouble(value);
        }
        if(type.isAssignableFrom(Boolean.class)){
            return Boolean.parseBoolean(value);
        }
        return value;
    }


}
