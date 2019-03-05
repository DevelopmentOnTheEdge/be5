package com.developmentontheedge.be5.server.services.impl.rows;

import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.query.util.DynamicPropertyMeta;
import com.developmentontheedge.be5.query.util.QueryUtils;
import com.developmentontheedge.be5.server.model.table.RawCellModel;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.GLUE_COLUMN_PREFIX;

/**
 * Parses properties in terms of tables.
 */
class PropertiesToRowTransformer
{
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    private SimpleDateFormat timestampFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    /**
     * Returns a row identifier or empty string if the given properties contains no identifier.
     */
    String getRowId(DynamicPropertySet properties)
    {
        Object idObject = properties.getValue(DatabaseConstants.ID_COLUMN_LABEL);
        return idObject != null ? String.valueOf(idObject) : null;
    }

    List<RawCellModel> collectCells(DynamicPropertySet properties)
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
            boolean hidden = QueryUtils.shouldBeSkipped(property);
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
        options.remove("nosort");
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
