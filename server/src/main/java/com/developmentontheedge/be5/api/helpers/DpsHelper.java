package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.be5.annotations.DirtyRealization;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.impl.EmptyRequest;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.SqlColumnType;
import com.developmentontheedge.be5.metadata.util.Strings2;
import com.developmentontheedge.be5.util.Utils;
import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.developmentontheedge.sql.format.Ast;
import com.google.common.collect.ImmutableList;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.*;
import static com.developmentontheedge.be5.metadata.model.SqlColumnType.TYPE_KEY;


public class DpsHelper
{
    private static final Logger log = Logger.getLogger(DpsHelper.class.getName());

    private static final List<String> insertSpecialColumns = ImmutableList.<String>builder()
            .add(WHO_INSERTED_COLUMN_NAME)
            .add(WHO_MODIFIED_COLUMN_NAME)
            .add(CREATION_DATE_COLUMN_NAME)
            .add(MODIFICATION_DATE_COLUMN_NAME)
            .add(IP_INSERTED_COLUMN_NAME)
            .add(IP_MODIFIED_COLUMN_NAME)
            .add(IS_DELETED_COLUMN_NAME)
            .build();

    private static final List<String> updateSpecialColumns = ImmutableList.<String>builder()
            .add(WHO_MODIFIED_COLUMN_NAME)
            .add(MODIFICATION_DATE_COLUMN_NAME)
            .add(IP_MODIFIED_COLUMN_NAME)
            .build();

    private Meta meta;
    private UserAwareMeta userAwareMeta;
    private OperationHelper operationHelper;

    public DpsHelper(Meta meta, OperationHelper operationHelper, UserAwareMeta userAwareMeta)
    {
        this.meta = meta;
        this.userAwareMeta = userAwareMeta;
        this.operationHelper = operationHelper;
    }

//    public DynamicPropertySet getDps(Entity entity, ResultSet resultSet)
//    {
//        DynamicPropertySet dps = getDps(entity);
//
//        return setValuesAndAddColumns(entity, dps, resultSet);
//    }

//    public DynamicPropertySet getDpsForColumns(Entity entity, Collection<String> columnNames, ResultSet resultSet)
//    {
//        DynamicPropertySet dps = getDpsForColumns(entity, columnNames);
//        return setValues(dps, resultSet);
//    }

    public DynamicPropertySet getDpsWithoutAutoIncrement(Entity entity, ResultSet resultSet)
    {
        DynamicPropertySet dps = getDpsWithoutAutoIncrement(entity);
        return setValues(dps, resultSet);
    }

    public DynamicPropertySet getDpsWithoutAutoIncrement(Entity entity, Map<String, ? super Object> values)
    {
        DynamicPropertySet dps = getDpsWithoutAutoIncrement(entity);
        return setValues(dps, values);
    }

    public DynamicPropertySet getDpsWithoutAutoIncrement(Entity entity)
    {
        if(meta.getColumn(entity, entity.getPrimaryKey()) != null && meta.getColumn(entity, entity.getPrimaryKey()).isAutoIncrement())
        {
            return getDpsWithoutColumns(entity, Collections.singletonList(entity.getPrimaryKey()));
        }
        return getDps(entity);
    }

    public DynamicPropertySet getDps(Entity entity)
    {
        return getDpsWithoutColumns(entity, Collections.emptyList());
    }

    public DynamicPropertySet getDpsWithoutColumns(Entity entity, Collection<String> excludedColumns)
    {
        Map<String, ColumnDef> columns = meta.getColumns(entity);
        DynamicPropertySet dps = new DynamicPropertySetSupport();

        ArrayList<String> excludedColumnsList = new ArrayList<>(excludedColumns);
        for (Map.Entry<String, ColumnDef> entry: columns.entrySet())
        {
            if(!excludedColumnsList.contains(entry.getKey()))
            {
                DynamicProperty dynamicProperty = getDynamicPropertyWithoutTags(entry.getValue());
                addTags(dynamicProperty, entry.getValue());
                dps.add(dynamicProperty);
                excludedColumnsList.remove(entry.getKey());
            }
        }

        for(String propertyName: excludedColumnsList)
        {
            log.warning("Column " + propertyName + " not found in " + entity.getName());
        }
        return dps;
    }

    public DynamicPropertySet getDpsForColumns(Entity entity, Collection<String> columnNames, Map<String, ? super Object> presetValues)
    {
        DynamicPropertySet dps = getDpsForColumns(entity, columnNames);
        return setValues(dps, presetValues);
    }

    public DynamicPropertySet getDpsForColumns(Entity entity, Collection<String> columnNames)
    {
        Map<String, ColumnDef> columns = meta.getColumns(entity);

        DynamicPropertySet dps = getDpsForColumnsWithoutTags(entity, columnNames);

        for(String propertyName: columnNames)
        {
            ColumnDef columnDef = columns.get(propertyName);
            if(columnDef != null)addTags(dps.getProperty(propertyName), columnDef);
        }

        return dps;
    }

    public void addDynamicProperties(DynamicPropertySet dps, Entity entity, Collection<String> propertyNames)
    {
        Map<String, ColumnDef> columns = meta.getColumns(entity);

        for(String propertyName: propertyNames)
        {
            ColumnDef columnDef = columns.get(propertyName);
            DynamicProperty dynamicProperty = getDynamicPropertyWithoutTags(columnDef);
            addTags(dynamicProperty, columnDef);

            dps.add(dynamicProperty);
        }
    }

    public DynamicPropertySet getDpsForColumnsWithoutTags(Entity entity, Collection<String> columnNames, Map<String, ? super Object> presetValues)
    {
        DynamicPropertySet dps = getDpsForColumnsWithoutTags(entity, columnNames);
        return setValues(dps, presetValues);
    }

    public DynamicPropertySet getDpsForColumnsWithoutTags(Entity entity, Collection<String> columnNames)
    {
        Map<String, ColumnDef> columns = meta.getColumns(entity);

        DynamicPropertySet dps = new DynamicPropertySetSupport();
        for(String propertyName: columnNames)
        {
            ColumnDef columnDef = columns.get(propertyName);
            if(columnDef != null)
            {
                DynamicProperty dynamicProperty = getDynamicPropertyWithoutTags(columnDef);
                dps.add(dynamicProperty);
            }
            else
            {
                throw Be5Exception.internal("Entity '" + entity.getName() + "' not contain column " + propertyName);
            }
        }

        return dps;
    }


    public DynamicProperty getDynamicPropertyWithoutTags(ColumnDef columnDef)
    {
        DynamicProperty dp = new DynamicProperty(columnDef.getName(), meta.getColumnType(columnDef));
        dp.setDisplayName(userAwareMeta.getColumnTitle(columnDef.getEntity().getName(), columnDef.getName() ));

        if(columnDef.getDefaultValue() != null)
        {
            dp.setValue(meta.getColumnDefaultValue(columnDef));
        }

        if(columnDef.isCanBeNull() ||
            (columnDef.getTypeString().equals(SqlColumnType.TYPE_BOOL) && columnDef.getDefaultValue() != null) )
        {
            dp.setCanBeNull(true);
        }

        if(columnDef.getName().endsWith(HIDDEN_COLUMN_PREFIX))dp.setHidden(true);

        return dp;
    }

    public void addTags(DynamicProperty dp, ColumnDef columnDef)
    {
        if(columnDef.getType().getTypeName().equals(SqlColumnType.TYPE_BOOL)){
            dp.setAttribute(BeanInfoConstants.TAG_LIST_ATTR, operationHelper.getTagsYesNo());
        }
        else if(columnDef.getType().getEnumValues() != Strings2.EMPTY)
        {
            dp.setAttribute(BeanInfoConstants.TAG_LIST_ATTR, operationHelper.getTagsFromEnum(columnDef));
        }
        else if(columnDef.getTableTo() != null && meta.getEntity(columnDef.getTableTo()) != null )
        {
            //todo get real request instead EmptyRequest
            dp.setAttribute(BeanInfoConstants.TAG_LIST_ATTR,
                    operationHelper.getTagsFromSelectionView(new EmptyRequest(), columnDef.getTableTo()));
        }
    }

    public DynamicPropertySet setValues(DynamicPropertySet dps, DynamicPropertySet values)
    {
        for(DynamicProperty valueProperty : values)
        {
            DynamicProperty property = dps.getProperty(valueProperty.getName());
            if(property != null)
            {
                property.setValue(valueProperty.getValue());
            }
        }
        return dps;
    }

    public DynamicPropertySet setValues(DynamicPropertySet dps, Map<String, ?> values)
    {
        for (Map.Entry<String, ?> entry : values.entrySet())
        {
            DynamicProperty property = dps.getProperty(entry.getKey());
            if(property != null && !property.isReadOnly())
            {
                dps.setValue(entry.getKey(), entry.getValue());
            }
        }
        return dps;
    }

    public DynamicPropertySet setValues(DynamicPropertySet dps, ResultSet resultSet)
    {
        try
        {
            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++)
            {
                String name = metaData.getColumnName(i);

                DynamicProperty property = dps.getProperty(name);
                if(property != null) {
                    property.setValue(DpsRecordAdapter.getSqlValue(property.getType(), resultSet, i));
                }
            }
        }
        catch (SQLException e)
        {
            throw Be5Exception.internal(e);
        }

        return dps;
    }

    public void addUpdateSpecialColumns(Entity entity, DynamicPropertySet dps)
    {
        addSpecialColumns(entity, dps, updateSpecialColumns);
    }

    public void addInsertSpecialColumns(Entity entity, DynamicPropertySet dps)
    {
        addSpecialColumns(entity, dps, insertSpecialColumns);
    }

    private void addSpecialColumns(Entity entity, DynamicPropertySet dps, List<String> specialColumns)
    {
        Map<String, ColumnDef> columns = meta.getColumns(entity);
        Timestamp currentTime = new Timestamp(new Date().getTime());

        for(String propertyName: specialColumns)
        {
            ColumnDef columnDef = columns.get(propertyName);
            if (columnDef != null)
            {
                Object value = getSpecialColumnsValue(propertyName, currentTime);
                if (dps.getProperty(propertyName) == null)
                {
                    DynamicProperty newProperty = new DynamicProperty(propertyName, value.getClass(), value);
                    newProperty.setHidden(true);
                    dps.add(newProperty);
                }
                else
                {
                    dps.setValue(propertyName, value);
                }
            }
        }
    }

    private Object getSpecialColumnsValue(String propertyName, Timestamp currentTime)
    {
        if(CREATION_DATE_COLUMN_NAME.equals(propertyName))return currentTime;
        if(MODIFICATION_DATE_COLUMN_NAME.equals(propertyName))return currentTime;

        if(WHO_INSERTED_COLUMN_NAME.equals(propertyName))return UserInfoHolder.getUserName();
        if(WHO_MODIFIED_COLUMN_NAME.equals(propertyName))return UserInfoHolder.getUserName();

        if(IS_DELETED_COLUMN_NAME.equals(propertyName))return "no";

        if(IP_INSERTED_COLUMN_NAME.equals(propertyName))return UserInfoHolder.getRemoteAddr();
        if(IP_MODIFIED_COLUMN_NAME.equals(propertyName))return UserInfoHolder.getRemoteAddr();

        throw Be5Exception.internal("Not support: " + propertyName);
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
//        return db.select(sql, DpsRecordAdapter::createDps);
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
//        return db.select(sql, DpsRecordAdapter::createDps, id);
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
        //todo remove property not contain in entity and log warning, as in checkDpsColumns
        //and add to generateUpdateSqlForOneKey

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

    public String generateUpdateSqlForConditions(Entity entity, DynamicPropertySet dps, Map<String, ? super Object> conditions)
    {
        Map<Object, Object> valuePlaceholders = StreamSupport.stream(dps.spliterator(), false)
                .collect(toLinkedMap(DynamicProperty::getName, x -> "?"));

        return Ast.update(entity.getName()).set(valuePlaceholders)
                .where(conditions).format();
    }

//    public String generateUpdateSqlForManyKeys(Entity entity, DynamicPropertySet dps, int count)
//    {
//        Map<Object, Object> valuePlaceholders = StreamSupport.stream(dps.spliterator(), false)
//                .collect(toLinkedMap(DynamicProperty::getName, x -> "?"));
//
//        return Ast.update(entity.getName()).set(valuePlaceholders)
//                .whereInPredicate(entity.getPrimaryKey(), count).format();
//    }

    public String generateDelete(Entity entity, Map<String, ? super Object> conditions)
    {
        Map<String, ColumnDef> columns = meta.getColumns(entity);
        if(columns.containsKey( IS_DELETED_COLUMN_NAME ))
        {
            LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
            values.put(IS_DELETED_COLUMN_NAME, "?");
            if( columns.containsKey( WHO_MODIFIED_COLUMN_NAME     ))values.put(WHO_MODIFIED_COLUMN_NAME, "?");
            if( columns.containsKey( MODIFICATION_DATE_COLUMN_NAME))values.put(MODIFICATION_DATE_COLUMN_NAME, "?");
            if( columns.containsKey( IP_MODIFIED_COLUMN_NAME      ))values.put(IP_MODIFIED_COLUMN_NAME, "?");

            return Ast.update(entity.getName()).set(values).where(conditions).format();
        }
        else
        {
            return Ast.delete(entity.getName()).where(conditions).format();
        }
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

        String whereSql = " WHERE " + entity.getPrimaryKey() + " IN " + Utils.inClause(count);
        return sql + whereSql;
    }

    public Object[] getDeleteSpecialValues(Entity entity)
    {
        Map<String, ColumnDef> columns = meta.getColumns(entity);
        Timestamp currentTime = new Timestamp(new Date().getTime());
        List<Object> list = new ArrayList<>();

        if(columns.containsKey( IS_DELETED_COLUMN_NAME ))
        {
            list.add("yes");
            if( columns.containsKey( WHO_MODIFIED_COLUMN_NAME     ))list.add(UserInfoHolder.getUserName());
            if( columns.containsKey( MODIFICATION_DATE_COLUMN_NAME))list.add(currentTime);
            if( columns.containsKey( IP_MODIFIED_COLUMN_NAME      ))list.add(UserInfoHolder.getRemoteAddr());
        }
        return list.toArray();
    }

//    private Object[] castToType(SqlColumnType type, Object[] ids)
//    {
//        Object[] castedIds = new Object[ids.length];
//        for (int i = 0; i < ids.length; i++)
//        {
//            castedIds[i] = castToType(type, ids[i]);
//        }
//        return castedIds;
//    }
//
//    @DirtyRealization(comment = "Use Utils.changeType")
//    private Object castToType(SqlColumnType type, Object id)
//    {
//        if(type.isIntegral() || type.getTypeName().equals(TYPE_KEY)){
//            return Long.parseLong(id.toString());
//        }
//        return id;
//    }

    @DirtyRealization(comment = "refactoring, castPrimaryKey ? add method for one, for many.")
    public Object[] castToTypePrimaryKey(Entity entity, Object[] ids)
    {
        SqlColumnType type = meta.getColumn(entity, entity.getPrimaryKey()).getType();
        if(type.isIntegral() || type.getTypeName().equals(TYPE_KEY))
        {
            return (Object[])Utils.changeType(ids, Long[].class);
        }
        else
        {
            if(ids instanceof Number[])throw Be5Exception.internal("Type should not be a Number");
        }
        return ids;
    }

    public Object castToTypePrimaryKey(Entity entity, Object id)
    {
        SqlColumnType type = meta.getColumn(entity, entity.getPrimaryKey()).getType();
        if(type.isIntegral() || type.getTypeName().equals(TYPE_KEY))
        {
            return Utils.changeType(id, Long.class);
        }
        else
        {
            if(id instanceof Number)throw Be5Exception.internal("Type should not be a Number");
        }

        return id;
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

    public void checkDpsColumns(Entity entity, DynamicPropertySet dps)
    {
        StringBuilder errorMsg = new StringBuilder();
        Map<String, ColumnDef> columns = meta.getColumns(entity);

        for (ColumnDef column : columns.values())
        {
            if (!column.isCanBeNull() && !column.isAutoIncrement() && column.getDefaultValue() == null
                    && !dps.hasProperty(column.getName()))
            {
                errorMsg.append("Dps not contain notNull column '").append(column.getName()).append("\n");
            }
        }

        for (DynamicProperty property : dps)
        {
            if (!columns.keySet().contains(property.getName()))
            {
                errorMsg.append("Entity not contain column '").append(property.getName()).append("\n");
            }
        }

        if(!errorMsg.toString().isEmpty())
        {
            throw Be5Exception.internal("Dps columns errors for entity '" + entity.getName() + "'\n"+ errorMsg);
        }
    }

    public DynamicProperty getLabel(String text)
    {
        return getLabel(text, "infoLabel");
    }

    public DynamicProperty getLabel(String text, String name)
    {
        DynamicProperty label = new DynamicProperty(name, String.class, text);
        label.setAttribute(BeanInfoConstants.LABEL_FIELD, true);
        return label;
    }

    public DynamicProperty getLabelRaw(String text)
    {
        return getLabelRaw(text, "infoLabel");
    }

    public DynamicProperty getLabelRaw(String text, String name)
    {
        DynamicProperty label = getLabel(text, name);
        label.setAttribute(BeanInfoConstants.RAW_VALUE, true);

        return label;
    }

    public DynamicPropertySet getDpsWithLabelANDNotSubmitted(String text)
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport();
        dps.add(getLabel(text, "infoLabel"));
        DynamicProperty notSubmitted = new DynamicProperty("notSubmitted", String.class, null);
        notSubmitted.setHidden(true);
        dps.add(notSubmitted);

        return dps;
    }

    public DynamicPropertySet getDpsWithLabelRawANDNotSubmitted(String text)
    {
        DynamicPropertySet dps = getDpsWithLabelANDNotSubmitted(text);
        dps.getProperty("infoLabel").setAttribute(BeanInfoConstants.RAW_VALUE, true);

        return dps;
    }
}
