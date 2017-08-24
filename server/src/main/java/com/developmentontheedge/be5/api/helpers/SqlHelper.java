package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.SqlColumnType;
import com.developmentontheedge.be5.metadata.util.Strings2;
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


public class SqlHelper
{
    private Meta meta;
    private OperationHelper operationHelper;

    public SqlHelper(Meta meta, OperationHelper operationHelper)
    {
        this.meta = meta;
        this.operationHelper = operationHelper;
    }

    public DynamicPropertySet getDps(Entity entity, ResultSet resultSet)
    {
        DynamicPropertySet dps = getDps(entity);

        return setDpsValues(dps, resultSet);
    }

    public DynamicPropertySet getDpsForValues(Entity entity, Map<String, String> values, ResultSet resultSet)
    {
        DynamicPropertySet dps = getDpsForValues(entity, values);

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
        if(meta.getColumn(entity, entity.getPrimaryKey()) != null && meta.getColumn(entity, entity.getPrimaryKey()).isAutoIncrement())
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

    public DynamicPropertySet getDpsForValues(Entity entity, Map<String, String> values)
    {
        Map<String, ColumnDef> columns = meta.getColumns(entity);

        DynamicPropertySet dps = new DynamicPropertySetSupport();

        for (Map.Entry<String, ColumnDef> entry: columns.entrySet())
        {
            if(values.containsKey(entry.getKey()))
            {
                dps.add(getDynamicProperty(entry.getValue()));
            }
        }
        return dps;
    }

    private DynamicProperty getDynamicProperty(ColumnDef columnDef)
    {
        DynamicProperty dynamicProperty = new DynamicProperty(columnDef.getName(), meta.getColumnType(columnDef));

        if(columnDef.getDefaultValue() != null)
        {
            dynamicProperty.setAttribute(BeanInfoConstants.DEFAULT_VALUE, meta.getColumnDefaultValue(columnDef));
        }

        if(columnDef.isCanBeNull())dynamicProperty.setCanBeNull(true);

        if(columnDef.getType().getTypeName().equals(SqlColumnType.TYPE_BOOL)){
            dynamicProperty.setAttribute(BeanInfoConstants.TAG_LIST_ATTR, operationHelper.getTagsYesNo());
        }
        else if(columnDef.getType().getEnumValues() != Strings2.EMPTY)
        {
            dynamicProperty.setAttribute(BeanInfoConstants.TAG_LIST_ATTR, operationHelper.getTagsFromEnum(columnDef));
        }
        else if(columnDef.hasReference()){
//            dynamicProperty.setAttribute(BeanInfoConstants.TAG_LIST_ATTR,
//                    operationHelper.getTagsFromSelectionView(null, columnDef.getTableTo()));
        }


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

    private void updateSpecialColumns(DynamicPropertySet dps)
    {
        Timestamp currentTime = new Timestamp(new Date().getTime());

        setValue(dps, WHO_MODIFIED_COLUMN_NAME, UserInfoHolder.getUserName());
        setValue(dps, MODIFICATION_DATE_COLUMN_NAME, currentTime);
        setValue(dps, IP_MODIFIED_COLUMN_NAME, UserInfoHolder.getRemoteAddr());
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

//    @Deprecated
//    public String paramsToCondition( Entity entity, Map<?,?> values )
//    {
//        String cond = "";
//        for( Map.Entry<?,?> entry : values.entrySet() )
//        {
//            if( !"".equals( cond ) )
//            {
//                cond += " AND ";
//            }
//            String column = entry.getKey().toString();
//            Object value = entry.getValue();
//            if( value instanceof Object[] )
//            {
//                cond += "" + column +
//                        " IN " + Utils.toInClause(singletonList(value), meta.isNumericColumn( entity, column ) );
//                continue;
//            }
//
//            String op = " = ";
//            if( value instanceof String && ( ( String )value ).endsWith( "%" ) )
//            {
//                op = " LIKE ";
//            }
//            cond += "" + column +
//                    ( value == null ? " IS NULL " :
//                            op + value );
//        }
//
//        return cond;
//    }

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

    public String generateUpdateSqlForOneKey(Entity entity, DynamicPropertySet dps)
    {
        Map<Object, Object> valuePlaceholders = StreamSupport.stream(dps.spliterator(), false)
                .collect(toLinkedMap(DynamicProperty::getName, x -> "?"));

        return Ast.update(entity.getName()).set(valuePlaceholders)
                .where(Collections.singletonMap(entity.getPrimaryKey(), "?")).format();
    }

    public String generateUpdateSqlForConditions(Entity entity, DynamicPropertySet dps, Map<String, String> conditions)
    {
        Map<Object, Object> valuePlaceholders = StreamSupport.stream(dps.spliterator(), false)
                .collect(toLinkedMap(DynamicProperty::getName, x -> "?"));

        return Ast.update(entity.getName()).set(valuePlaceholders)
                .where(conditions).format();
    }

    public String generateUpdateSqlForManyKeys(Entity entity, DynamicPropertySet dps, int count)
    {
        Map<Object, Object> valuePlaceholders = StreamSupport.stream(dps.spliterator(), false)
                .collect(toLinkedMap(DynamicProperty::getName, x -> "?"));

        return Ast.update(entity.getName()).set(valuePlaceholders)
                .whereInPredicate(entity.getPrimaryKey(), count).format();
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
    //
    //todo Use Utils.changeType()
    public Object castToTypePrimaryKey(Entity entity, Object id)
    {
        ColumnDef primaryKeyColumn = meta.getColumn(entity, entity.getPrimaryKey());
        return castToType(primaryKeyColumn.getType(), id);
    }

    public String inClause(int count){
        return "(" + IntStream.range(0, count).mapToObj(x -> "?").collect(Collectors.joining(", ")) + ")";
    }

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
