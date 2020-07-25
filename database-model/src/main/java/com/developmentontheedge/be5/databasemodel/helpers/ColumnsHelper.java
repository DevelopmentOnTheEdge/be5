package com.developmentontheedge.be5.databasemodel.helpers;

import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.security.UserInfoProvider;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.CREATION_DATE_COLUMN_NAME;
import static com.developmentontheedge.be5.metadata.DatabaseConstants.IP_INSERTED_COLUMN_NAME;
import static com.developmentontheedge.be5.metadata.DatabaseConstants.IP_MODIFIED_COLUMN_NAME;
import static com.developmentontheedge.be5.metadata.DatabaseConstants.IS_DELETED_COLUMN_NAME;
import static com.developmentontheedge.be5.metadata.DatabaseConstants.MODIFICATION_DATE_COLUMN_NAME;
import static com.developmentontheedge.be5.metadata.DatabaseConstants.WHO_INSERTED_COLUMN_NAME;
import static com.developmentontheedge.be5.metadata.DatabaseConstants.WHO_MODIFIED_COLUMN_NAME;
import static com.developmentontheedge.be5.metadata.DatabaseConstants.specialColumns;


public class ColumnsHelper
{
    static final List<String> insertSpecialColumns = specialColumns;

    static final List<String> updateSpecialColumns = ImmutableList.<String>builder()
            .add(WHO_MODIFIED_COLUMN_NAME)
            .add(MODIFICATION_DATE_COLUMN_NAME)
            .add(IP_MODIFIED_COLUMN_NAME)
            .build();

    private final Meta meta;
    private final UserInfoProvider userInfoProvider;

    @Inject
    public ColumnsHelper(Meta meta, UserInfoProvider userInfoProvider)
    {
        this.meta = meta;
        this.userInfoProvider = userInfoProvider;
    }

    public Map<String, Object> withUpdateSpecialColumns(Entity entity, Map<String, ?> values)
    {
        Map<String, Object> map = new LinkedHashMap<>(values);
        addSpecialColumns(entity, map, ColumnsHelper.updateSpecialColumns);
        return map;
    }

    public void addInsertSpecialColumns(Entity entity, Map<String, Object> values)
    {
        addSpecialColumns(entity, values, ColumnsHelper.insertSpecialColumns);
    }

    private void addSpecialColumns(Entity entity, Map<String, Object> values, List<String> specialColumns)
    {
        Map<String, ColumnDef> columns = meta.getColumns(entity);
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());

        for (String propertyName : specialColumns)
        {
            ColumnDef columnDef = columns.get(propertyName);
            if (columnDef != null)
            {
                Object value = getSpecialColumnsValue(propertyName, currentTime);
                if (!values.containsKey(propertyName))
                {
                    values.put(propertyName, value);
                }
            }
        }
    }

    public Object getSpecialColumnsValue(String propertyName, Timestamp currentTime)
    {
        if (CREATION_DATE_COLUMN_NAME.equals(propertyName)) return currentTime;
        if (MODIFICATION_DATE_COLUMN_NAME.equals(propertyName)) return currentTime;

        if (WHO_INSERTED_COLUMN_NAME.equals(propertyName)) return userInfoProvider.getUserName();
        if (WHO_MODIFIED_COLUMN_NAME.equals(propertyName)) return userInfoProvider.getUserName();

        if (IS_DELETED_COLUMN_NAME.equals(propertyName)) return "no";

        if (IP_INSERTED_COLUMN_NAME.equals(propertyName)) return userInfoProvider.getRemoteAddr();
        if (IP_MODIFIED_COLUMN_NAME.equals(propertyName)) return userInfoProvider.getRemoteAddr();

        throw Be5Exception.internal("Not support: " + propertyName);
    }

    public Map<String, ?> addDeleteSpecialValues(Entity entity, Map<String, Object> values)
    {
        Map<String, ColumnDef> columns = meta.getColumns(entity);
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());

        if (columns.containsKey(IS_DELETED_COLUMN_NAME))
        {
            values.put(IS_DELETED_COLUMN_NAME, "yes");
            if (columns.containsKey(WHO_MODIFIED_COLUMN_NAME))
                values.put(WHO_MODIFIED_COLUMN_NAME, userInfoProvider.getUserName());
            if (columns.containsKey(MODIFICATION_DATE_COLUMN_NAME))
                values.put(MODIFICATION_DATE_COLUMN_NAME, currentTime);
            if (columns.containsKey(IP_MODIFIED_COLUMN_NAME))
                values.put(IP_MODIFIED_COLUMN_NAME, userInfoProvider.getRemoteAddr());
        }
        return values;
    }

    public void checkDpsColumns(Entity entity, Map<String, ?> values)
    {
        StringBuilder errorMsg = new StringBuilder();
        Map<String, ColumnDef> columns = meta.getColumns(entity);

        for (ColumnDef column : columns.values())
        {
            if (!column.isCanBeNull() && !column.isAutoIncrement() && column.getDefaultValue() == null
                    && !values.containsKey(column.getName()))
            {
                errorMsg.append("DPS does not contain notNull column '").append(column.getName()).append("'\n");
            }
        }

        values.entrySet().removeIf(e -> !columns.keySet().contains(e.getKey()));

        if (!errorMsg.toString().isEmpty())
        {
            throw Be5Exception.internal("DPS columns errors for modelElements '" + entity.getName() + "'\n" + errorMsg);
        }
    }
}
