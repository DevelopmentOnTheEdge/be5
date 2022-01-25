package com.developmentontheedge.be5.server.services.document.rows;

import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.database.util.DynamicPropertyMeta;
import com.developmentontheedge.be5.query.util.QueryUtils;
import com.developmentontheedge.be5.security.UserInfo;
import com.developmentontheedge.be5.server.model.table.RawCellModel;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

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

    List<RawCellModel> collectCells(DynamicPropertySet properties, UserInfo userInfo)
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
                    formatValue(property, userInfo),
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

    private Object formatValue(DynamicProperty property, UserInfo userInfo)
    {
        Object value = property.getValue();
        if (value == null) return null;

        if (java.sql.Date.class.equals(property.getType()))
        {
            return dateFormatter.format(value);
        }

        if (java.sql.Time.class.equals(property.getType()))
        {
            String timestamp = timestampFormatter.format(value);
            if (timestamp.startsWith("01.01.1970"))
            {
                timestamp = timestamp.substring(11);
            }
            return timestamp;
        }

        if (java.sql.Timestamp.class.equals(property.getType()))
        {
//          for differents locale
//          value = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, userInfo.getLocale())
//                    .format(adjustForUsersTimeZone((java.sql.Timestamp) value, userInfo));
//          simple variant
//          TODO it's only for java.sql.Timestamp property type, but most timestamp field uses formated as strings in views
//          value = adjustForUsersTimeZone((java.sql.Timestamp) value, userInfo);
            return timestampFormatter.format(value);
        }

        return value;
    }

    private java.util.Date adjustForUsersTimeZone(java.util.Date date, UserInfo ui)
    {
        if (ui == null || ui.getTimeZone() == null)
        {
            return date;
        }
        Calendar cal = Calendar.getInstance();
        if (date != null)
        {
            cal.setTime(date);
        }
        if (ui.getTimeZone().getRawOffset() > TimeZone.getDefault().getRawOffset())
        {
            cal.add(Calendar.MILLISECOND, ui.getTimeZone().getRawOffset() - TimeZone.getDefault().getRawOffset());
        } else if (TimeZone.getDefault().getRawOffset() > ui.getTimeZone().getRawOffset())
        {
            cal.add(Calendar.MILLISECOND, -(TimeZone.getDefault().getRawOffset() - ui.getTimeZone().getRawOffset()));
        }
        return cal.getTime();
    }
}
