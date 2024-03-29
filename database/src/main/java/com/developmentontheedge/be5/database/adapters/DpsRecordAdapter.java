package com.developmentontheedge.be5.database.adapters;

import com.developmentontheedge.be5.database.util.SqlUtils;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.database.QRec;
import com.developmentontheedge.be5.database.util.DynamicPropertyMeta;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import one.util.streamex.IntStreamEx;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.EXTRA_HEADER_COLUMN_PREFIX;

public class DpsRecordAdapter
{
    private static final String COLUMN_REF_IDX_PROPERTY = "columnRefIdx";

    public static <T extends DynamicPropertySet> T addDp(T dps, ResultSet resultSet) throws SQLException
    {
        DynamicProperty[] schema = DpsRecordAdapter.createSchema(resultSet.getMetaData());

        try
        {
            for (int i = 0; i < schema.length; i++)
            {
                DynamicProperty dp = schema[i];
                Object refIdxObj = dp.getAttribute(COLUMN_REF_IDX_PROPERTY);
                if (refIdxObj instanceof Integer)
                {
                    int refIdx = (int) refIdxObj;
                    if (refIdx >= 0)
                    {
                        Map<String, Map<String, String>> tags = new TreeMap<>();
                        BeTagParser.parseTags(tags, resultSet.getString(i + 1));
                        DynamicPropertyMeta.add(schema[refIdx], tags);
                        dp.setAttribute(COLUMN_REF_IDX_PROPERTY, -1);
                    }
                    continue;
                }
                Object val = SqlUtils.getSqlValue(dp.getType(), resultSet, i + 1);
                DynamicProperty property = QRec.cloneProperty(dp);
                property.setValue(val);
                dps.add(property);
            }
            return dps;
        }
        catch (Exception e)
        {
            throw Be5Exception.internal(e);
        }
    }

    private static DynamicProperty[] createSchema(ResultSetMetaData metaData)
    {
        return createSchema(metaData, SqlUtils::getTypeClass);
    }

    private static DynamicProperty[] createSchema(ResultSetMetaData metaData, Function<Integer, Class<?>> getTypeClassFun)
    {
        try
        {
            int count = metaData.getColumnCount();
            DynamicProperty[] schema = new DynamicProperty[count];
            Set<String> names = new HashSet<>();
            for (int i = 1; i <= count; i++)
            {
                String columnLabel = metaData.getColumnLabel(i);
                if (columnLabel.startsWith(EXTRA_HEADER_COLUMN_PREFIX))
                {
                    String refName = columnLabel.substring(1);
                    int refId = IntStreamEx.ofIndices(schema, dp -> dp != null && dp.getName().equals(refName))
                            .findAny().orElseThrow(() -> new RuntimeException("no previous column with name " + refName));
                    DynamicProperty dp = new DynamicProperty(columnLabel, String.class);
                    dp.setAttribute(COLUMN_REF_IDX_PROPERTY, refId);
                    schema[i - 1] = dp;
                    continue;
                }
                String[] parts = columnLabel.split(EXTRA_HEADER_COLUMN_PREFIX, 2);
                String name = getUniqueName(names, parts[0]);
                Class<?> clazz = getTypeClassFun.apply(metaData.getColumnType(i));
                DynamicProperty dp = new DynamicProperty(name, clazz);
                Map<String, Map<String, String>> tags = new TreeMap<>();
                if (parts.length == 2)
                    BeTagParser.parseTags(tags, parts[1]);
                DynamicPropertyMeta.set(dp, tags);
                schema[i - 1] = dp;
            }
            return schema;
        }
        catch (SQLException e)
        {
            throw Be5Exception.internal(e);
        }
    }

    private static String getUniqueName(Set<String> names, String baseName)
    {
        String name = baseName;
        int i = 0;
        while (names.contains(name))
        {
            name = baseName + " (" + (++i) + ")";
        }
        return name;
    }
}
