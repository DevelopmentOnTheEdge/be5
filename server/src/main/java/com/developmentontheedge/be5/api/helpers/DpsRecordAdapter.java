package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.impl.DpsExecutorImpl;
import com.developmentontheedge.be5.components.impl.model.BeTagParser;
import com.developmentontheedge.be5.components.impl.model.DynamicPropertyMeta;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import one.util.streamex.IntStreamEx;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class DpsRecordAdapter
{
    private static final String COLUMN_REF_IDX_PROPERTY = "columnRefIdx";

    public static DynamicPropertySet createDps(ResultSet resultSet)
    {
        return createDps(resultSet, (a,b)->{});
    }

    public static DynamicPropertySet createDps(ResultSet resultSet, DpsExecutorImpl.MetaProcessor metaProcessor)
    {
        try {
            DynamicProperty[] schema = createSchema(resultSet.getMetaData());
            DynamicPropertySet row = new DynamicPropertySetSupport();
            for( int i = 0; i < schema.length; i++ )
            {
                DynamicProperty dp = schema[i];
                Object refIdxObj = dp.getAttribute(COLUMN_REF_IDX_PROPERTY);
                if(refIdxObj instanceof Integer) {
                    int refIdx = (int) refIdxObj;
                    if(refIdx >= 0) {
                        Map<String, Map<String, String>> tags = new TreeMap<>();
                        BeTagParser.parseTags(tags, resultSet.getString(i+1));
                        DynamicPropertyMeta.add(schema[refIdx], tags);
                        dp.setAttribute(COLUMN_REF_IDX_PROPERTY, -1);
                    }
                    continue;
                }
                Object val = getSqlValue( dp.getType(), resultSet, i + 1 );
                Map<String, Map<String, String>> metaInfo = DynamicPropertyMeta.get(dp);
                metaProcessor.process(val, metaInfo);
                DynamicProperty property = DynamicPropertySetSupport.cloneProperty( dp );
                property.setValue( val );
                row.add( property );
            }
            return row;
        }catch (Exception e){
            throw Be5Exception.internal(e);
        }
    }

    public static Object getSqlValue(Class<?> clazz, ResultSet rs, int idx)
    {
        try{
            Object object = rs.getObject(idx);
            return clazz.cast(object);
        }catch (Exception e){
            throw Be5Exception.internal(e);
        }
    }

    public static DynamicProperty[] createSchema(ResultSetMetaData metaData)
    {
        try {
            int count = metaData.getColumnCount();
            DynamicProperty[] schema = new DynamicProperty[count];
            Set<String> names = new HashSet<>();
            // TODO: support ";ColumnName" declarations
            for (int i = 1; i <= count; i++) {
                String columnLabel = metaData.getColumnLabel(i);
                if (columnLabel.startsWith(";")) {
                    String refName = columnLabel.substring(1);
                    int refId = IntStreamEx.ofIndices(schema, dp -> dp != null && dp.getName().equals(refName))
                            .findAny().orElseThrow(() -> Be5Exception.internal("no previous column with name " + refName));
                    DynamicProperty dp = new DynamicProperty(columnLabel, String.class);
                    dp.setAttribute(COLUMN_REF_IDX_PROPERTY, refId);
                    dp.setHidden(true);
                    schema[i - 1] = dp;
                    continue;
                }
                String[] parts = columnLabel.split(";", 2);
                String name = getUniqueName(names, parts[0]);
                Class<?> clazz = getTypeClass(metaData.getColumnType(i));
                DynamicProperty dp = new DynamicProperty(name, clazz);
                if (name.startsWith(DatabaseConstants.HIDDEN_COLUMN_PREFIX)) {
                    dp.setHidden(true);
                }
                Map<String, Map<String, String>> tags = new TreeMap<>();
                if (parts.length == 2)
                    BeTagParser.parseTags(tags, parts[1]);
                DynamicPropertyMeta.set(dp, tags);
                // TODO: support various types, attributes, tags, meta
                schema[i - 1] = dp;
            }
            return schema;
        }catch (SQLException e){
            throw Be5Exception.internal(e);
        }
    }

    public static Class<?> getTypeClass(int columnType)
    {
        switch( columnType )
        {
            case Types.BIGINT:
                return Long.class;
            case Types.INTEGER:
                return Integer.class;
            case Types.SMALLINT:
                return Short.class;
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.DECIMAL:
            case Types.REAL:
            case Types.NUMERIC:
                return Double.class;
            case Types.BOOLEAN:
                return Boolean.class;
            case Types.DATE:
                return Date.class;
            case Types.TIME:
                return Time.class;
            case Types.TIMESTAMP:
                return Timestamp.class;
            case Types.CLOB:
                return Clob.class;
            case Types.BLOB:
                return Blob.class;
            default:
                return String.class;
        }
    }

    private static String getUniqueName(Set<String> names, String baseName)
    {
        String name = baseName;
        int i = 0;
        while( names.contains( name ) )
        {
            name = baseName + " (" + ( ++i ) + ")";
        }
        return name;
    }
}
