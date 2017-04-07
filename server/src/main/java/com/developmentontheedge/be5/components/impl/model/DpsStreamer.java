package com.developmentontheedge.be5.components.impl.model;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.exceptions.impl.Be5ErrorCode;
import com.developmentontheedge.be5.api.services.DatabaseService;
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
import java.util.logging.Logger;


public class DpsStreamer
{
    private static final Logger log = Logger.getLogger(DpsStreamer.class.getName());
    private static final String COLUMN_REF_IDX_PROPERTY = "columnRefIdx";

    @FunctionalInterface
    public static interface MetaProcessor
    {
        void process(Object value, Map<String, Map<String, String>> meta);
    }

    private final DatabaseService databaseService;

    public DpsStreamer(DatabaseService databaseService)
    {
        this.databaseService = databaseService;
    }

    /**
     * Streams an SQL query result as a sequence of dynamic property sets.
     */
    public StreamEx<DynamicPropertySet> stream(String sql, MetaProcessor metaProcessor)
    {
        ResultSet rs = null;
        try
        {
            rs = databaseService.executeQuery( sql , true);
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
                        action.accept(createRow( finalRs, createSchema( finalRs.getMetaData() ), metaProcessor ));
                        return true;
                    }
                    catch( Throwable t )
                    {
                        databaseService.close( finalRs );
                        throw Be5ErrorCode.INTERNAL_ERROR.rethrow(log, t);
                    }
                }
            } ).onClose( () -> databaseService.close( finalRs ) );
        }
        catch( Exception e )
        {
            databaseService.close( rs );
            throw Be5ErrorCode.INTERNAL_ERROR.rethrow(log, e);
        }
    }

    protected DynamicPropertySet createRow(ResultSet resultSet, DynamicProperty[] schema, MetaProcessor metaProcessor) throws Exception
    {
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

    public static DynamicProperty[] createSchema(ResultSetMetaData metaData) throws SQLException
    {
        int count = metaData.getColumnCount();
        DynamicProperty[] schema = new DynamicProperty[count];
        Set<String> names = new HashSet<>();
        // TODO: support ";ColumnName" declarations
        for( int i = 1; i <= count; i++ )
        {
            String columnLabel = metaData.getColumnLabel( i );
            if(columnLabel.startsWith(";")) {
                String refName = columnLabel.substring(1);
                int refId = IntStreamEx.ofIndices(schema, dp -> dp != null && dp.getName().equals(refName))
                        .findAny().orElseThrow(() -> Be5Exception.internal("No previous column with name "+refName));
                DynamicProperty dp = new DynamicProperty( columnLabel, String.class );
                dp.setAttribute(COLUMN_REF_IDX_PROPERTY, refId);
                dp.setHidden(true);
                schema[i - 1] = dp;
                continue;
            }
            String[] parts = columnLabel.split( ";", 2 );
            String name = getUniqName( names, parts[0] );
            Class<?> clazz = getTypeClass( metaData.getColumnType( i ) );
            DynamicProperty dp = new DynamicProperty( name, clazz );
            if( name.startsWith( DatabaseConstants.HIDDEN_COLUMN_PREFIX ) )
            {
                dp.setHidden( true );
            }
            Map<String, Map<String, String>> tags = new TreeMap<>();
            if(parts.length == 2)
                BeTagParser.parseTags( tags, parts[1] );
            DynamicPropertyMeta.set(dp, tags);
            // TODO: support various types, attributes, tags, meta 
            schema[i - 1] = dp;
        }
        return schema;
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

    private static String getUniqName(Set<String> names, String baseName)
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
