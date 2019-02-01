package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.config.CoreUtils;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.model.SqlBoolColumnType;
import com.developmentontheedge.be5.query.model.ColumnModel;
import com.developmentontheedge.be5.query.model.RawCellModel;
import com.developmentontheedge.be5.query.util.DynamicPropertyMeta;
import com.developmentontheedge.be5.query.util.TableUtils;
import com.developmentontheedge.be5.security.UserInfo;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.GLUE_COLUMN_PREFIX;
import static com.developmentontheedge.be5.query.util.TableUtils.shouldBeSkipped;

/**
 * Parses properties in terms of tables.
 */
class PropertiesToRowTransformer
{
    private final String entityName;
    private final String queryName;
    private final DynamicPropertySet properties;
    private final UserInfo userInfo;
    private final CoreUtils coreUtils;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    private SimpleDateFormat timestampFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    /**
     * @param properties represent a row
     */
    PropertiesToRowTransformer(String entityName, String queryName, DynamicPropertySet properties, UserInfo userInfo,
                               CoreUtils coreUtils)
    {
        this.entityName = entityName;
        this.queryName = queryName;
        this.properties = properties;
        this.userInfo = userInfo;
        this.coreUtils = coreUtils;
    }

    /**
     * Returns a row identifier or empty string if the given properties contains no identifier.
     */
    String getRowId()
    {
        Object idObject = properties.getValue(DatabaseConstants.ID_COLUMN_LABEL);
        return idObject != null ? String.valueOf(idObject) : null;
    }

    /**
     * Constructs and collects columns. Note that this list contains no hidden columns (of any kind).
     */
    List<ColumnModel> collectColumns()
    {
        List<ColumnModel> columns = new ArrayList<>();

        for (DynamicProperty property : properties)
        {
            if (!shouldBeSkipped(property))
            {
                String quick = getQuickOptionState(property);
                columns.add(new ColumnModel(
                        property.getName(),
                        property.getDisplayName(),
                        quick
                ));
            }
        }

        return columns;
    }

    private String getQuickOptionState(DynamicProperty property)
    {
        Map<String, String> quickOption = DynamicPropertyMeta.get(property).get("quick");
        if (quickOption == null) return null;

        String savedQuick = (String) coreUtils.getColumnSettingForUser(entityName, queryName, property.getName(),
                                                        userInfo.getUserName()).get("quick");
        if (savedQuick != null) return savedQuick;

        if ("true".equals(quickOption.get("visible")))
            return SqlBoolColumnType.YES;
        else
            return SqlBoolColumnType.NO;
    }

    List<RawCellModel> collectCells()
    {
        for (DynamicProperty property : properties)
        {
            if (property.getName().startsWith(GLUE_COLUMN_PREFIX))
            {
                String targetName = property.getName().substring(GLUE_COLUMN_PREFIX.length());
                DynamicProperty tp = properties.getProperty(targetName);

                Object val = tp.getValue();
                if (val instanceof String && property.getValue() != null)
                {
                    tp.setValue(val.toString() + property.getValue());
                }
                property.setHidden(true);
            }
        }

        List<RawCellModel> cells = new ArrayList<>();

        for (DynamicProperty property : properties)
        {
            boolean hidden = TableUtils.shouldBeSkipped(property);
            Map<String, Map<String, String>> options = removeUnnecessaryCellOptions(DynamicPropertyMeta.get(property));
            cells.add(new RawCellModel(
                    property.getName(),
                    property.getDisplayName(),
                    formatValue(property),
                    options,
                    hidden
            ));
        }

        return cells;
    }

    private Map<String, Map<String, String>> removeUnnecessaryCellOptions(Map<String, Map<String, String>> options)
    {
        options.remove("quick");
        return options;
    }

    private Object formatValue(DynamicProperty property)
    {
        Object value = property.getValue();
        if (value == null) return null;

        if (property.getType() == java.sql.Date.class)
        {
            return dateFormatter.format(value);
        }

        if (property.getType() == java.sql.Time.class)
        {
            String timestamp = timestampFormatter.format(value);
            if (timestamp.startsWith("01.01.1970"))
            {
                timestamp = timestamp.substring(11);
            }
            return timestamp;
        }

        if (property.getType() == java.sql.Timestamp.class)
        {
            return timestampFormatter.format(value);
        }

        return value;
    }
}
