package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.DpsExecutor;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.components.impl.model.BeTagParser;
import com.developmentontheedge.be5.components.impl.model.DynamicPropertyMeta;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;

import java.sql.Date;
import java.sql.*;
import java.util.*;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;


public class DpsExecutorImpl implements DpsExecutor
{
    private static final String COLUMN_REF_IDX_PROPERTY = "columnRefIdx";

    @FunctionalInterface
    public interface MetaProcessor
    {
        void process(Object value, Map<String, Map<String, String>> meta);
    }

    private final DatabaseService databaseService;
    private final SqlService db;

    public DpsExecutorImpl(DatabaseService databaseService, SqlService db)
    {
        this.databaseService = databaseService;
        this.db = db;
    }

    @Override
    public List<DynamicPropertySet> list(String sql, MetaProcessor metaProcessor) {
        return db.selectList(sql, rs -> getDps(rs, metaProcessor));
    }

//    @Override
//    public DynamicPropertySet get(String sql)
//    {
//        return get(sql, (a,b)->{});
//    }
//
//    @Override
//    public DynamicPropertySet get(String sql, MetaProcessor metaProcessor)
//    {
//        return db.select(sql, rs -> getDps(rs, createSchema(rs.getMetaData()), metaProcessor));
//    }

    @Override
    public StreamEx<DynamicPropertySet> stream(String sql, MetaProcessor metaProcessor)
    {
        ResultSet rs = null;
        try
        {
            rs = databaseService.executeQuery(sql);
            ResultSet finalRs = rs;
            return StreamEx.of( new AbstractSpliterator<DynamicPropertySet>(Long.MAX_VALUE,
                    Spliterator.ORDERED | Spliterator.IMMUTABLE)
            {
                @Override
                public boolean tryAdvance(Consumer<? super DynamicPropertySet> action)
                {
                    try
                    {
                        if( !finalRs.next() )
                        {
                            databaseService.close( finalRs );
                            return false;
                        }
                        action.accept(getDps( finalRs, metaProcessor ));
                        return true;
                    }
                    catch( Throwable t )
                    {
                        databaseService.close( finalRs );
                        throw new RuntimeException(t);
                    }
                }
            } ).onClose( () -> databaseService.close( finalRs ) );
        }
        catch( Exception e )
        {
            databaseService.close( rs );
            throw new RuntimeException(e);
        }
    }

    public DynamicPropertySet getDps(ResultSet resultSet){
        return getDps(resultSet, (a,b)->{});
    }

    public DynamicPropertySet getDps(ResultSet resultSet, MetaProcessor metaProcessor)
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

    private Object getSqlValue(Class<?> clazz, ResultSet rs, int idx) throws SQLException
    {
        if( clazz == String.class )
            return rs.getString( idx );
        if( clazz == Integer.class )
            return rs.getInt( idx );
        if( clazz == Long.class )
            return rs.getLong( idx );
        if( clazz == Boolean.class )
            return rs.getBoolean( idx );
        if( clazz == Double.class )
            return rs.getDouble( idx );
        if( clazz == Float.class )
            return rs.getFloat( idx );
        if( clazz == Date.class )
            return rs.getDate( idx );
        if( clazz == Time.class )
            return rs.getTimestamp( idx );
        throw new IllegalArgumentException( clazz.getName() );
    }

    @Override
    public DynamicProperty[] createSchema(ResultSetMetaData metaData)
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
                            .findAny().orElseThrow(() -> Be5Exception.internal("No previous column with name " + refName));
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

    private static Class<?> getTypeClass(int columnType)
    {
        switch( columnType )
        {
            case Types.BIGINT:
                return Long.class;
            case Types.INTEGER:
                return Integer.class;
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
            case Types.TIMESTAMP:
                return Time.class;
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
