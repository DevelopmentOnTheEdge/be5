package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.metadata.Utils;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.SqlColumnType;
import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.developmentontheedge.sql.format.Ast;
import com.google.common.collect.ObjectArrays;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.*;
import static com.developmentontheedge.be5.metadata.model.SqlColumnType.TYPE_KEY;
import static java.util.Collections.singletonList;

public class SqlHelper
{
    private Meta meta;

    public SqlHelper(Meta meta)
    {
        this.meta = meta;
    }

    public DynamicPropertySet getDps(Entity entity, ResultSet resultSet)
    {
        DynamicPropertySet dps = getDps(entity);

        return setDpsValues(dps, resultSet);
    }

    public DynamicPropertySet getDpsWithoutAutoIncrement(Entity entity, ResultSet resultSet)
    {
        DynamicPropertySet dps = getDpsWithoutAutoIncrement(entity);

        return setDpsValues(dps, resultSet);
    }

    public DynamicPropertySet setDpsValues(DynamicPropertySet dps, ResultSet resultSet)
    {
        try
        {
            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++)
            {
                DynamicProperty property = dps.getProperty(metaData.getColumnName(i));
                if( property!= null)
                    property.setValue(DpsHelper.getSqlValue(property.getType(), resultSet, i));
            }
        }
        catch (SQLException e)
        {
            throw Be5Exception.internal(e);
        }

        return dps;
    }

    public DynamicPropertySet getDpsWithoutAutoIncrement(Entity entity)
    {
        DynamicPropertySet dps = getDps(entity);
        if(!entity.getPrimaryKey().contains("dummy") && meta.getColumn(entity, entity.getPrimaryKey()).isAutoIncrement())
        {
            dps.remove(entity.getPrimaryKey());
        }
        return dps;
    }

    public DynamicPropertySet getDps(Entity entity)
    {
        Map<String, ColumnDef> columns = meta.getColumns(entity);

        DynamicPropertySet dps = new DynamicPropertySetSupport();

        for (Map.Entry<String, ColumnDef> entry: columns.entrySet())
        {
            dps.add(getDynamicProperty(entry.getValue()));
        }
        return dps;
    }

    private DynamicProperty getDynamicProperty(ColumnDef columnDef)
    {
        DynamicProperty dynamicProperty = new DynamicProperty(columnDef.getName(), meta.getColumnType(columnDef));
        if(columnDef.isCanBeNull())dynamicProperty.setCanBeNull(true);

        return dynamicProperty;
    }

    public void setValues(DynamicPropertySet dps, Entity entity, Map<String, ?> values)
    {
        for (DynamicProperty property : dps)
        {
            if (property.getValue() == null) property.setValue(values.get(property.getName()));
            if (property.getValue() == null) property.setValue(property.getAttribute(BeanInfoConstants.DEFAULT_VALUE));
            if (!entity.getName().startsWith("_") && property.getValue() == null) property.setValue(meta.getColumnDefaultValue(entity, property.getName()));
        }

        setSpecialColumnsIfNullValue(dps);
    }

    public void updateValuesWithSpecial(DynamicPropertySet dps, Map<String, ?> values)
    {
        for (Map.Entry<String, ?> entry: values.entrySet())
        {
            DynamicProperty property = dps.getProperty(entry.getKey());
            if( property!= null)
                property.setValue(entry.getValue());
        }

        updateSpecialColumns(dps);
    }

    private void setSpecialColumnsIfNullValue(DynamicPropertySet dps)
    {
        Timestamp currentTime = new Timestamp(new Date().getTime());

        setValueIfNullValue(dps, WHO_INSERTED_COLUMN_NAME, UserInfoHolder.getUserName());
        setValueIfNullValue(dps, WHO_MODIFIED_COLUMN_NAME, UserInfoHolder.getUserName());

        setValueIfNullValue(dps, CREATION_DATE_COLUMN_NAME, currentTime);
        setValueIfNullValue(dps, MODIFICATION_DATE_COLUMN_NAME, currentTime);

        setValueIfNullValue(dps, IS_DELETED_COLUMN_NAME, "no");

        setValueIfNullValue(dps, IP_INSERTED_COLUMN_NAME, UserInfoHolder.getRemoteAddr());
        setValueIfNullValue(dps, IP_MODIFIED_COLUMN_NAME, UserInfoHolder.getRemoteAddr());
    }

    private void updateSpecialColumns(DynamicPropertySet dps)
    {
        Timestamp currentTime = new Timestamp(new Date().getTime());

        setValue(dps, WHO_MODIFIED_COLUMN_NAME, UserInfoHolder.getUserName());
        setValue(dps, MODIFICATION_DATE_COLUMN_NAME, currentTime);
        setValue(dps, IP_MODIFIED_COLUMN_NAME, UserInfoHolder.getRemoteAddr());
    }

    private void setValueIfNullValue(DynamicPropertySet dps, String name, Object value)
    {
        DynamicProperty property = dps.getProperty(name);
        if(property != null && property.getValue() == null){
            property.setValue(value);
        }
    }

    private void setValue(DynamicPropertySet dps, String name, Object value)
    {
        DynamicProperty property = dps.getProperty(name);
        if(property != null){
            property.setValue(value);
        }
    }

    public Object[] getValues(DynamicPropertySet dps)
    {
        return StreamSupport.stream(dps.spliterator(), false)
                .map(DynamicProperty::getValue).toArray();
    }

//    public String getConditionsSql(Entity entity, String primaryKey, Map<?, ?> conditions ) throws SQLException
//    {
//        StringBuilder sql = new StringBuilder( paramsToCondition( entity, conditions ) );
//
//        if( meta.getColumn( entity, DatabaseConstants.IS_DELETED_COLUMN_NAME ) != null )
//        {
//            sql.append( " AND " + DatabaseConstants.IS_DELETED_COLUMN_NAME + " != 'yes'" );
//        }
//        return sql.toString();
//    }
//
//    public DynamicPropertySet getRecordByConditions(Entity entity, String primaryKey, Map<?, ?> conditions ) throws SQLException
//    {
//        String tableName = entity.getName();
//
//        String sql = "SELECT * FROM " + tableName + " WHERE 1 = 1 AND "
//                      + getConditionsSql( entity, primaryKey, conditions );
//
//        return db.select(sql, DpsHelper::createDps);
//    }
//
//    public DynamicPropertySet getRecordById( Entity entity, Long id )
//    {
//        return getRecordById( entity, id, Collections.emptyMap() );
//    }
//
//    public DynamicPropertySet getRecordById( Entity entity, Long id, Map<String, Object> conditions)
//    {
//        String sql = "SELECT * FROM " + entity.getName()
//                + " WHERE " + entity.getPrimaryKey() + " = ?";
//
//        if( !conditions.isEmpty() )
//        {
//            sql += " AND " + paramsToCondition( entity, conditions );
//        }
//
//        if( meta.getColumn( entity, DatabaseConstants.IS_DELETED_COLUMN_NAME ) != null )
//        {
//            sql += " AND " + DatabaseConstants.IS_DELETED_COLUMN_NAME + " != 'yes'";
//        }
//
//        return db.select(sql, DpsHelper::createDps, id);
//    }

    @Deprecated
    public String paramsToCondition( Entity entity, Map<?,?> values )
    {
        String cond = "";
        for( Map.Entry<?,?> entry : values.entrySet() )
        {
            if( !"".equals( cond ) )
            {
                cond += " AND ";
            }
            String column = entry.getKey().toString();
            Object value = entry.getValue();
            if( value instanceof Object[] )
            {
                cond += "" + column +
                        " IN " + Utils.toInClause(singletonList(value), meta.isNumericColumn( entity, column ) );
                continue;
            }

            String op = " = ";
            if( value instanceof String && ( ( String )value ).endsWith( "%" ) )
            {
                op = " LIKE ";
            }
            cond += "" + column +
                    ( value == null ? " IS NULL " :
                            op + value );
        }

        return cond;
    }

    public String generateInsertSql(Entity entity, DynamicPropertySet dps)
    {
        Object[] columns = StreamSupport.stream(dps.spliterator(), false)
                .map(DynamicProperty::getName)
                .toArray(Object[]::new);

        Object[] valuePlaceholders = StreamSupport.stream(dps.spliterator(), false)
                .map(x -> "?")
                .toArray(Object[]::new);

        return Ast.insert(entity.getName()).fields(columns).values(valuePlaceholders).format();

        // Oracle trick for auto-generated IDs
//            if( connector.isOracle() && colName.equalsIgnoreCase( pk ) )
//            {
//                if( entity.equalsIgnoreCase( value ) || JDBCRecordAdapter.AUTO_IDENTITY.equals( value ) )
//                {
//                    sql.append( "beIDGenerator.NEXTVAL" );
//                }
//                else if( ( entity + "_" + pk + "_seq" ).equalsIgnoreCase( value ) )
//                {
//                    sql.append( value ).append( ".NEXTVAL" );
//                }
//                else
//                {
//                    //in case of not autoincremented PK
//                    justAddValueToQuery( connector, entity, prop, value, sql );
//                }
//            }
//            else if( connector.isOracle() && !connector.isOracle8() &&
//                     "CLOB".equals( prop.getAttribute( JDBCRecordAdapter.DATABASE_TYPE_NAME ) ) )
//            {
//                sql.append( OracleDatabaseAnalyzer.makeClobValue( connector, value ) );
//            }
        //else
//            {
//                justAddValueToQuery( databaseService, "entity", prop, value, sql );
//            }


    }

    public String generateUpdateSql(Entity entity, DynamicPropertySet dps)
    {
        Map<Object, Object> valuePlaceholders = StreamSupport.stream(dps.spliterator(), false)
                .collect(toLinkedMap(DynamicProperty::getName, x -> "?"));

        return Ast.update(entity.getName()).set(valuePlaceholders)
                .where(Collections.singletonMap(entity.getPrimaryKey(), "?")).format();
    }

    public String generateDeleteInSql(Entity entity, int count) {
        String sql;
        Map<String, ColumnDef> columns = meta.getColumns(entity);
        if(columns.containsKey( IS_DELETED_COLUMN_NAME ))
        {
            sql = "UPDATE " + entity.getName() + " SET " + IS_DELETED_COLUMN_NAME + " = ?";
            if( columns.containsKey( WHO_MODIFIED_COLUMN_NAME ))
            {
                sql += ", " + WHO_MODIFIED_COLUMN_NAME + " = ?";
            }
            if( columns.containsKey( MODIFICATION_DATE_COLUMN_NAME))
            {
                sql += ", " + MODIFICATION_DATE_COLUMN_NAME + " = ?";
            }
            if( columns.containsKey( IP_MODIFIED_COLUMN_NAME ))
            {
                sql += ", " + IP_MODIFIED_COLUMN_NAME + " = ?";
            }
        }
        else
        {
            sql = "DELETE FROM " + entity.getName();
        }

        //add support sql IN in where
//        Ast.delete(entity.getName())
//                .where();

        String whereSql = " WHERE " + entity.getPrimaryKey() + " IN " + inClause(count);
        return sql + whereSql;
    }

    public Object[] getDeleteValuesWithSpecial(Entity entity, Object[] ids)
    {
        Map<String, ColumnDef> columns = meta.getColumns(entity);
        Timestamp currentTime = new Timestamp(new Date().getTime());
        List<Object> list = new ArrayList<>();

        if(columns.containsKey( IS_DELETED_COLUMN_NAME ))
        {
            list.add("yes");
            if( columns.containsKey( WHO_MODIFIED_COLUMN_NAME ))
            {
                list.add(UserInfoHolder.getUserName());
            }
            if( columns.containsKey( MODIFICATION_DATE_COLUMN_NAME))
            {
                list.add(currentTime);
            }
            if( columns.containsKey( IP_MODIFIED_COLUMN_NAME ))
            {
                list.add(UserInfoHolder.getRemoteAddr());
            }
        }

        ColumnDef primaryKeyColumn = meta.getColumn(entity, entity.getPrimaryKey());

        return ObjectArrays.concat(list.toArray(), castToType(primaryKeyColumn.getType(), ids), Object.class);
    }

    private Object[] castToType(SqlColumnType type, Object[] ids)
    {
        Object[] castedIds = new Object[ids.length];
        for (int i = 0; i < ids.length; i++)
        {
            castedIds[i] = castToType(type, ids[i]);
        }
        return castedIds;
    }

    private Object castToType(SqlColumnType type, Object id)
    {
        if(type.isIntegral() || type.getTypeName().equals(TYPE_KEY)){
            return Long.parseLong(id.toString());
        }
        return id;
    }

    //todo refactoring, castPrimaryKey ? add method for one, for many.
    // Use meta.getColumns
    public Object castToTypePrimaryKey(Entity entity, Object id)
    {
        ColumnDef primaryKeyColumn = meta.getColumn(entity, entity.getPrimaryKey());
        return castToType(primaryKeyColumn.getType(), id);
    }

    private String quoteStr(String str)
    {
        return "'" + str + "'";
    }

    public String inClause(int count){
        return "(" + IntStream.range(0, count).mapToObj(x -> "?").collect(Collectors.joining(", ")) + ")";
    }

//    public static String safeValue( DatabaseService connector, DynamicProperty prop )
//    {
//        Object val = prop.getValue();
////
////        if( val instanceof Wrapper )
////        {
////             val = ( (Wrapper)val ).unwrap();
////        }
////
//        String value;
//        if( val == null )
//        {
//            value = "";
//        }
//        else if( val instanceof java.sql.Timestamp )
//        {
//            value = new SimpleDateFormat( connector.getRdbms() == Rdbms.ORACLE ? "yyyy-MM-dd HH:mm:ss" : "yyyy-MM-dd HH:mm:ss.SSS" ).format( val );
//        }
//        else if( val instanceof java.sql.Time )
//        {
//            value = new SimpleDateFormat( "HH:mm:ss" ).format( val );
//        }
//        else if( val instanceof Date)
//        {
//            java.sql.Date sqlDate = new java.sql.Date( ( ( Date )val ).getTime() );
//            value = sqlDate.toString();
//        }
//        else if( val instanceof Calendar)
//        {
//            value = new SimpleDateFormat( connector.getRdbms() == Rdbms.ORACLE ? "yyyy-MM-dd HH:mm:ss" : "yyyy-MM-dd HH:mm:ss.SSS" ).format( ( ( Calendar )val ).getTime() );
//        }
//        else if( val instanceof Boolean ) // BIT
//        {
//            value = Boolean.TRUE.equals( val ) ? "1" : "0";
//        }
//        // must be encrypted?
////        else if( prop.getName().toLowerCase().startsWith( ENCRYPT_COLUMN_PREFIX ) )
////        {
////            value = CryptoUtils.encrypt( val.toString() );
////            prop.setAttribute( PASSWORD_FIELD, Boolean.TRUE );
////        }
//        else
//        {
//            value = val.toString();
//
//            if( Number.class.isAssignableFrom( prop.getType() ) )
//            {
//                value = value.replace(",", ".");
//            }
//
///*          String orig = value;
//            String sizeStr = ( String )prop.getAttribute( COLUMN_SIZE_ATTR );
//            String encoding = connector.getEncoding();
//            if( value != null && value.length() > 0 && sizeStr != null && encoding != null )
//            {
//                if( connector.isOracle() )
//                {
//                    // truncate value since it causes exception
//                    int size = Integer.parseInt( sizeStr );
//                    byte bytes[] = value.getBytes( encoding );
//                    int length = size;
//                    if( length > bytes.length )
//                        length = bytes.length;
//                    value = new String( bytes, 0, length, encoding );
//
//                    // in case we got into middle of multi-byte char
//                    // From Google
//                    // Unicode 65533 is a substitute character for use when
//                    // a character is found that can't be output in the selected encoding
//                    char last = value.charAt( value.length() - 1 );
//                    if( (int)last == 65533 )
//                    {
//                        value = value.substring( 0, value.length() - 1 );
//                    }
//                }
//            }
//*/
//        }
//
//        if( "".equals( value ) && prop.isCanBeNull() )
//            value = null;
//
//        if( value == null && String.class.equals( prop.getType() ) && !prop.isCanBeNull() )
//            value = "";
//
//        return value;
//    }

    public static <T, K, U> Collector<T, ?, Map<K,U>> toLinkedMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper)
    {
        return Collectors.toMap(keyMapper, valueMapper,
                (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                },
                LinkedHashMap::new);
    }
}
