package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.be5.annotations.DirtyRealization;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.sql.DpsRecordAdapter;
import com.developmentontheedge.be5.api.validation.rule.ValidationRules;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.GroovyOperation;
import com.developmentontheedge.be5.metadata.model.JavaOperation;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.SqlColumnType;
import com.developmentontheedge.be5.metadata.model.base.BeModelElement;
import com.developmentontheedge.be5.metadata.util.Strings2;
import com.developmentontheedge.be5.util.ParseRequestUtils;
import com.developmentontheedge.be5.util.Utils;
import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.sql.format.Ast;
import com.developmentontheedge.sql.model.AstBeParameterTag;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;
import com.google.common.collect.ImmutableList;
import com.google.common.math.LongMath;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.developmentontheedge.be5.api.validation.rule.ValidationRules.range;
import static com.developmentontheedge.be5.api.validation.rule.ValidationRules.step;
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

    public <T extends DynamicPropertySet> T addDp(T dps, BeModelElement modelElements, ResultSet resultSet, Map<String, String> parameters)
    {
        addDp(dps, modelElements, parameters);
        return setValues(dps, resultSet);
    }

    public <T extends DynamicPropertySet> T addDpWithoutTags(T dps, BeModelElement modelElements, ResultSet resultSet)
    {
        addDpWithoutTags(dps, modelElements);
        return setValues(dps, resultSet);
    }

//    public DynamicPropertySet getDpsForColumns(BeModelElement modelElements, Collection<String> columnNames, ResultSet resultSet)
//    {
//        DynamicPropertySet dps = getDpsForColumns(modelElements, columnNames);
//        return setValues(dps, resultSet);
//    }

//    public DynamicPropertySet getDpsExcludeAutoIncrement(BeModelElement modelElements, ResultSet resultSet)
//    {
//        DynamicPropertySet dps = getDpsExcludeAutoIncrement(modelElements);
//        return setValues(dps, resultSet);
//    }

    public <T extends DynamicPropertySet> T addDpExcludeAutoIncrement(T dps, BeModelElement modelElements, Map<String, String> parameters, Map<String, ? super Object> values)
    {
        addDpExcludeAutoIncrement(dps, modelElements, parameters);
        return setValues(dps, values);
    }

    public <T extends DynamicPropertySet> T addDpExcludeAutoIncrement(T dps, BeModelElement modelElements, Map<String, String> parameters)
    {
        List<String> excludedColumns = Collections.emptyList();
        if(meta.getColumn(getEntity(modelElements), getEntity(modelElements).getPrimaryKey()) != null &&
           meta.getColumn(getEntity(modelElements), getEntity(modelElements).getPrimaryKey()).isAutoIncrement())
        {
            excludedColumns = Collections.singletonList(getEntity(modelElements).getPrimaryKey());
        }

        return addDpExcludeColumns(dps, modelElements, excludedColumns, parameters);
    }

    public <T extends DynamicPropertySet> T addDp(T dps, BeModelElement modelElements, Map<String, String> parameters)
    {
        return addDpExcludeColumns(dps, modelElements, Collections.emptyList(), parameters);
    }

    public <T extends DynamicPropertySet> T addDpWithoutTags(T dps, BeModelElement modelElements)
    {
        return addDpsExcludedColumnsWithoutTags(dps, modelElements, Collections.emptyList());
    }

    public <T extends DynamicPropertySet> T addDpExcludeColumns(T dps, BeModelElement modelElements, Collection<String> columnNames, Map<String, String> parameters)
    {
        addDpsExcludedColumnsWithoutTags(dps, modelElements, columnNames);

        return addTags(dps, modelElements, dps.asMap().keySet().stream().filter(i -> !columnNames.contains(i)).collect(Collectors.toList()), parameters);
    }

    private <T extends DynamicPropertySet> T addTags(T dps, BeModelElement modelElements, Collection<String> columnNames, Map<String, String> parameters)
    {
        Map<String, ColumnDef> columns = meta.getColumns(getEntity(modelElements));
        for(String propertyName: columnNames)
        {
            DynamicProperty property = dps.getProperty(propertyName);
            ColumnDef columnDef = columns.get(property.getName());
            if(columnDef != null)addTags(property, columnDef, parameters);
        }
        return dps;
    }

    public <T extends DynamicPropertySet> T addDpsExcludedColumnsWithoutTags(T dps, BeModelElement modelElements, Collection<String> excludedColumns)
    {
        Map<String, ColumnDef> columns = meta.getColumns(getEntity(modelElements));

        ArrayList<String> excludedColumnsList = new ArrayList<>(excludedColumns);
        for (Map.Entry<String, ColumnDef> entry: columns.entrySet())
        {
            if(!excludedColumnsList.contains(entry.getKey()))
            {
                DynamicProperty dynamicProperty = getDynamicPropertyWithoutTags(entry.getValue(), modelElements);
                dps.add(dynamicProperty);
            }
            excludedColumnsList.remove(entry.getKey());
        }

        for(String propertyName: excludedColumnsList)
        {
            log.warning("Column " + propertyName + " not found in " + modelElements.getName());
        }
        return dps;
    }

    public <T extends DynamicPropertySet> T addDpForColumns(T dps, BeModelElement modelElements, Collection<String> columnNames,
                                            Map<String, String> parameters, Map<String, ? super Object> presetValues)
    {
        addDpForColumns(dps, modelElements, columnNames, parameters);

        setValues(dps, presetValues);
        setOperationParams(dps, parameters);

        return dps;
    }

    public <T extends DynamicPropertySet> T addDpForColumns(T dps, BeModelElement modelElements, Collection<String> columnNames,
                                                            Map<String, String> parameters)
    {
        addDpForColumnsWithoutTags(dps, modelElements, columnNames);

        addTags(dps, modelElements, columnNames, parameters);

        return dps;
    }

    public <T extends DynamicPropertySet> T addDynamicProperties(T dps, BeModelElement modelElements, Collection<String> propertyNames, Map<String, String> parameters)
    {
        Map<String, ColumnDef> columns = meta.getColumns(getEntity(modelElements));

        for(String propertyName: propertyNames)
        {
            ColumnDef columnDef = columns.get(propertyName);
            DynamicProperty dynamicProperty = getDynamicPropertyWithoutTags(columnDef, modelElements);
            addTags(dynamicProperty, columnDef, parameters);

            dps.add(dynamicProperty);
        }
        return dps;
    }

    public <T extends DynamicPropertySet> T addDpForColumnsWithoutTags(T dps, BeModelElement modelElements, Collection<String> columnNames,
                                                                       Map<String, ? super Object> presetValues)
    {
        addDpForColumnsWithoutTags(dps, modelElements, columnNames);

        setValues(dps, presetValues);

        return dps;
    }

    public <T extends DynamicPropertySet> T addDpForColumnsWithoutTags(T dps, BeModelElement modelElements, Collection<String> columnNames)
    {
        Map<String, ColumnDef> columns = meta.getColumns(getEntity(modelElements));
        for(String propertyName: columnNames)
        {
            ColumnDef columnDef = columns.get(propertyName);
            if(columnDef != null)
            {
                DynamicProperty dynamicProperty = getDynamicPropertyWithoutTags(columnDef, modelElements);
                dps.add(dynamicProperty);
            }
            else
            {
                throw Be5Exception.internal("Entity '" + modelElements.getName() + "' not contain column " + propertyName);
            }
        }
        return dps;
    }

    public <T extends DynamicPropertySet> T addParamsFromQuery(T dps, BeModelElement modelElements, Query query, Map<String, String> parameters)
    {
        AstStart ast;
        try
        {
            ast = SqlQuery.parse(meta.getQueryCode(query, UserInfoHolder.getCurrentRoles()));
        }
        catch (RuntimeException e)
        {
            log.log(Level.SEVERE, "SqlQuery.parse error: " , e);
            throw Be5Exception.internalInQuery(e, query);
        }

        List<String> usedParams = ast.tree().select(AstBeParameterTag.class).map(AstBeParameterTag::getName).toList();

        addDpForColumns(dps, modelElements, usedParams, parameters);

        return dps;
    }

    public DynamicProperty getDynamicPropertyWithoutTags(ColumnDef columnDef, BeModelElement modelElements)
    {
        DynamicProperty dp = new DynamicProperty(columnDef.getName(), meta.getColumnType(columnDef));

        if(modelElements.getClass() == Query.class)
        {
            dp.setDisplayName(userAwareMeta.getColumnTitle(
                    columnDef.getEntity().getName(),
                    modelElements.getName(),
                    columnDef.getName()
            ));
        }
        else if(modelElements.getClass() == Entity.class)
        {
            dp.setDisplayName(userAwareMeta.getColumnTitle(
                    columnDef.getEntity().getName(),
                    columnDef.getName()
            ));
        }
        else if(modelElements.getClass() == JavaOperation.class || modelElements.getClass() == GroovyOperation.class)
        {
            dp.setDisplayName(userAwareMeta.getLocalizedOperationField(
                    columnDef.getEntity().getName(),
                    modelElements.getName(),
                    columnDef.getName()
            ));
        }

        if(columnDef.getDefaultValue() != null)
        {
            dp.setValue(meta.getColumnDefaultValue(columnDef));
        }

        if(columnDef.isCanBeNull() ||
                (columnDef.getTypeString().equals(SqlColumnType.TYPE_BOOL) && columnDef.getDefaultValue() != null) )
        {
            dp.setCanBeNull(true);
        }

        if(SqlColumnType.TYPE_TEXT.equals(columnDef.getType().getTypeName()))
        {
            dp.setAttribute(BeanInfoConstants.EXTRA_ATTRS, new String[][]{{"inputType", "textArea"}});
        }

        if(SqlColumnType.TYPE_VARCHAR.equals(columnDef.getType().getTypeName()) ||
           SqlColumnType.TYPE_CHAR.equals(columnDef.getType().getTypeName())){
            dp.setAttribute(BeanInfoConstants.COLUMN_SIZE_ATTR, columnDef.getType().getSize());
        }

        if(SqlColumnType.TYPE_DECIMAL.equals(columnDef.getType().getTypeName()))
        {
            int size = columnDef.getType().getSize();
            dp.setAttribute(BeanInfoConstants.VALIDATION_RULES, Arrays.asList(
                    getRange(size, false),
                    step(getPrecision(columnDef.getType().getPrecision()))
            ));
        }

        if(SqlColumnType.TYPE_CURRENCY.equals(columnDef.getType().getTypeName()))
        {
            dp.setAttribute(BeanInfoConstants.VALIDATION_RULES, Arrays.asList(
                    getRange(columnDef.getType().getSize(), false),
                    step(0.01)
            ));
        }

        if(SqlColumnType.TYPE_INT.equals(columnDef.getType().getTypeName()) ||
           SqlColumnType.TYPE_UINT.equals(columnDef.getType().getTypeName()))
        {
            boolean unsigned = SqlColumnType.TYPE_UINT.equals(columnDef.getType().getTypeName());

            dp.setAttribute(BeanInfoConstants.VALIDATION_RULES, Arrays.asList(
                    range(unsigned ? 0 : Integer.MIN_VALUE, Integer.MAX_VALUE),
                    step(1)
            ));
        }

        if(SqlColumnType.TYPE_BIGINT.equals(columnDef.getType().getTypeName()) ||
           SqlColumnType.TYPE_UBIGINT.equals(columnDef.getType().getTypeName()))
        {
            boolean unsigned = SqlColumnType.TYPE_UBIGINT.equals(columnDef.getType().getTypeName());

            dp.setAttribute(BeanInfoConstants.VALIDATION_RULES, Arrays.asList(
                    range(unsigned ? 0 : Long.MIN_VALUE, Long.MAX_VALUE),
                    step(1)
            ));
        }

        if(columnDef.getName().endsWith(HIDDEN_COLUMN_PREFIX))dp.setHidden(true);

        return dp;
    }

    public String getPrecision(int precision)
    {
        switch (precision){
            case 0 : return "1";
            case 1 : return "0.1";
            case 2 : return "0.01";
            case 3 : return "0.001";
            case 4 : return "1.0E-4";
            default:
                return "1.0E-" + Integer.toString(precision);
        }
    }

    public ValidationRules.Rule getRange(int size, boolean unsigned)
    {
        if(size <= 18){
            return range(unsigned ? 0 : -LongMath.pow(10, size), LongMath.pow(10, size));
        }else{
            return range(unsigned ? 0 : -Math.pow(10, size), Math.pow(10, size));
        }
    }

    public void addTags(DynamicProperty dp, ColumnDef columnDef, Map<String, String> parameters)
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
            dp.setAttribute(BeanInfoConstants.TAG_LIST_ATTR,
                    operationHelper.getTagsFromSelectionView(columnDef.getTableTo(), parameters));
        }
    }

    public <T extends DynamicPropertySet> T setValues(T dps, DynamicPropertySet values)
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

    public <T extends DynamicPropertySet> T setValues(T dps, Map<String, ?> values)
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

    public <T extends DynamicPropertySet> T setValues(T dps, ResultSet resultSet)
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

    public void addUpdateSpecialColumns(BeModelElement modelElements, DynamicPropertySet dps)
    {
        addSpecialColumns(modelElements, dps, updateSpecialColumns);
    }

    public void addInsertSpecialColumns(BeModelElement modelElements, DynamicPropertySet dps)
    {
        addSpecialColumns(modelElements, dps, insertSpecialColumns);
    }

    private void addSpecialColumns(BeModelElement modelElements, DynamicPropertySet dps, List<String> specialColumns)
    {
        Map<String, ColumnDef> columns = meta.getColumns(getEntity(modelElements));
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

//    public String getConditionsSql(BeModelElement modelElements, String primaryKey, Map<?, ?> conditions ) throws SQLException
//    {
//        StringBuilder sql = new StringBuilder( paramsToCondition( modelElements, conditions ) );
//
//        if( meta.getColumn( modelElements, DatabaseConstants.IS_DELETED_COLUMN_NAME ) != null )
//        {
//            sql.append( " AND " + DatabaseConstants.IS_DELETED_COLUMN_NAME + " != 'yes'" );
//        }
//        return sql.toString();
//    }
//
//    public DynamicPropertySet getRecordByConditions(BeModelElement modelElements, String primaryKey, Map<?, ?> conditions ) throws SQLException
//    {
//        String tableName = modelElements.getName();
//
//        String sql = "SELECT * FROM " + tableName + " WHERE 1 = 1 AND "
//                      + getConditionsSql( modelElements, primaryKey, conditions );
//
//        return db.select(sql, DpsRecordAdapter::createDps);
//    }
//
//    public DynamicPropertySet getRecordById( BeModelElement modelElements, Long id )
//    {
//        return getRecordById( modelElements, id, Collections.emptyMap() );
//    }
//
//    public DynamicPropertySet getRecordById( BeModelElement modelElements, Long id, Map<String, Object> conditions)
//    {
//        String sql = "SELECT * FROM " + modelElements.getName()
//                + " WHERE " + getEntity(modelElements).getPrimaryKey() + " = ?";
//
//        if( !conditions.isEmpty() )
//        {
//            sql += " AND " + paramsToCondition( modelElements, conditions );
//        }
//
//        if( meta.getColumn( modelElements, DatabaseConstants.IS_DELETED_COLUMN_NAME ) != null )
//        {
//            sql += " AND " + DatabaseConstants.IS_DELETED_COLUMN_NAME + " != 'yes'";
//        }
//
//        return db.select(sql, DpsRecordAdapter::createDps, id);
//    }

//    @Deprecated
//    public String paramsToCondition( BeModelElement modelElements, Map<?,?> values )
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
//                        " IN " + Utils.toInClause(singletonList(value), meta.isNumericColumn( modelElements, column ) );
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

    public String generateInsertSql(BeModelElement modelElements, DynamicPropertySet dps)
    {
        //todo remove property not contain in modelElements and log warning, as in checkDpsColumns
        //and add to generateUpdateSqlForOneKey

        Object[] columns = StreamSupport.stream(dps.spliterator(), false)
                .map(DynamicProperty::getName)
                .toArray(Object[]::new);

        Object[] valuePlaceholders = StreamSupport.stream(dps.spliterator(), false)
                .map(x -> "?")
                .toArray(Object[]::new);

        return Ast.insert(modelElements.getName()).fields(columns).values(valuePlaceholders).format();

        // Oracle trick for auto-generated IDs
//            if( connector.isOracle() && colName.equalsIgnoreCase( pk ) )
//            {
//                if( modelElements.equalsIgnoreCase( value ) || JDBCRecordAdapter.AUTO_IDENTITY.equals( value ) )
//                {
//                    sql.append( "beIDGenerator.NEXTVAL" );
//                }
//                else if( ( modelElements + "_" + pk + "_seq" ).equalsIgnoreCase( value ) )
//                {
//                    sql.append( value ).append( ".NEXTVAL" );
//                }
//                else
//                {
//                    //in case of not autoincremented PK
//                    justAddValueToQuery( connector, modelElements, prop, value, sql );
//                }
//            }
//            else if( connector.isOracle() && !connector.isOracle8() &&
//                     "CLOB".equals( prop.getAttribute( JDBCRecordAdapter.DATABASE_TYPE_NAME ) ) )
//            {
//                sql.append( OracleDatabaseAnalyzer.makeClobValue( connector, value ) );
//            }
        //else
//            {
//                justAddValueToQuery( databaseService, "modelElements", prop, value, sql );
//            }


    }

    public String generateUpdateSqlForOneKey(BeModelElement modelElements, DynamicPropertySet dps)
    {
        Map<Object, Object> valuePlaceholders = StreamSupport.stream(dps.spliterator(), false)
                .collect(Utils.toLinkedMap(DynamicProperty::getName, x -> "?"));

        return Ast.update(modelElements.getName()).set(valuePlaceholders)
                .where(Collections.singletonMap(getEntity(modelElements).getPrimaryKey(), "?")).format();
    }

    public String generateUpdateSqlForConditions(BeModelElement modelElements, DynamicPropertySet dps, Map<String, ? super Object> conditions)
    {
        Map<Object, Object> valuePlaceholders = StreamSupport.stream(dps.spliterator(), false)
                .collect(Utils.toLinkedMap(DynamicProperty::getName, x -> "?"));

        return Ast.update(modelElements.getName()).set(valuePlaceholders)
                .where(conditions).format();
    }

//    public String generateUpdateSqlForManyKeys(BeModelElement modelElements, DynamicPropertySet dps, int count)
//    {
//        Map<Object, Object> valuePlaceholders = StreamSupport.stream(dps.spliterator(), false)
//                .collect(toLinkedMap(DynamicProperty::getName, x -> "?"));
//
//        return Ast.update(modelElements.getName()).set(valuePlaceholders)
//                .whereInPredicate(getEntity(modelElements).getPrimaryKey(), count).format();
//    }

    public String generateDelete(BeModelElement modelElements, Map<String, ? super Object> conditions)
    {
        Map<String, ColumnDef> columns = meta.getColumns(getEntity(modelElements));
        if(columns.containsKey( IS_DELETED_COLUMN_NAME ))
        {
            LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
            values.put(IS_DELETED_COLUMN_NAME, "?");
            if( columns.containsKey( WHO_MODIFIED_COLUMN_NAME     ))values.put(WHO_MODIFIED_COLUMN_NAME, "?");
            if( columns.containsKey( MODIFICATION_DATE_COLUMN_NAME))values.put(MODIFICATION_DATE_COLUMN_NAME, "?");
            if( columns.containsKey( IP_MODIFIED_COLUMN_NAME      ))values.put(IP_MODIFIED_COLUMN_NAME, "?");

            return Ast.update(modelElements.getName()).set(values).where(conditions).format();
        }
        else
        {
            return Ast.delete(modelElements.getName()).where(conditions).format();
        }
    }

    public String generateDeleteInSql(BeModelElement modelElements, int count) {
        String sql;
        Map<String, ColumnDef> columns = meta.getColumns(getEntity(modelElements));
        if(columns.containsKey( IS_DELETED_COLUMN_NAME ))
        {
            sql = "UPDATE " + modelElements.getName() + " SET " + IS_DELETED_COLUMN_NAME + " = ?";
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
            sql = "DELETE FROM " + modelElements.getName();
        }

        //add support sql IN in where
//        Ast.delete(modelElements.getName())
//                .where();

        String whereSql = " WHERE " + getEntity(modelElements).getPrimaryKey() + " IN " + Utils.inClause(count);
        return sql + whereSql;
    }

    public Object[] getDeleteSpecialValues(BeModelElement modelElements)
    {
        Map<String, ColumnDef> columns = meta.getColumns(getEntity(modelElements));
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
    public Object[] castToTypePrimaryKey(BeModelElement modelElements, Object[] ids)
    {
        SqlColumnType type = meta.getColumn(getEntity(modelElements), getEntity(modelElements).getPrimaryKey()).getType();
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

    public Object castToTypePrimaryKey(BeModelElement modelElements, Object id)
    {
        SqlColumnType type = meta.getColumn(getEntity(modelElements), getEntity(modelElements).getPrimaryKey()).getType();
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

    public void checkDpsColumns(BeModelElement modelElements, DynamicPropertySet dps)
    {
        StringBuilder errorMsg = new StringBuilder();
        Map<String, ColumnDef> columns = meta.getColumns(getEntity(modelElements));

        for (ColumnDef column : columns.values())
        {
            if (!column.isCanBeNull() && !column.isAutoIncrement() && column.getDefaultValue() == null
                    && !dps.hasProperty(column.getName()))
            {
                errorMsg.append("Dps not contain notNull column '").append(column.getName()).append("'\n");
            }
        }

        for (DynamicProperty property : dps)
        {
            if (!columns.keySet().contains(property.getName()))
            {
                errorMsg.append("Entity not contain column '").append(property.getName()).append("'\n");
            }
        }

        if(!errorMsg.toString().isEmpty())
        {
            throw Be5Exception.internal("Dps columns errors for modelElements '" + modelElements.getName() + "'\n"+ errorMsg);
        }
    }

    public <T extends DynamicPropertySet> T addLabel(T dps, String text)
    {
        dps.add(getLabel("infoLabel", text));
        return dps;
    }

    public <T extends DynamicPropertySet> T addLabel(T dps, String name, String text)
    {
        dps.add(getLabel(name, text));
        return dps;
    }

    public <T extends DynamicPropertySet> T addLabelRaw(T dps, String text)
    {
        dps.add(getLabelRaw("infoLabel", text));
        return dps;
    }

    public <T extends DynamicPropertySet> T addLabelRaw(T dps, String name, String text)
    {
        dps.add(getLabelRaw(name, text));
        return dps;
    }

    public DynamicProperty getLabel(String text)
    {
        return getLabel("infoLabel", text);
    }

    public DynamicProperty getLabel(String name, String text)
    {
        DynamicProperty label = new DynamicProperty(name, String.class, text);
        label.setAttribute(BeanInfoConstants.LABEL_FIELD, true);
        label.setAttribute(BeanInfoConstants.CAN_BE_NULL, true);
        return label;
    }

    public DynamicProperty getLabelRaw(String text)
    {
        return getLabelRaw("infoLabel", text);
    }

    public DynamicProperty getLabelRaw(String name, String text)
    {
        DynamicProperty label = getLabel(name, text);
        label.setAttribute(BeanInfoConstants.RAW_VALUE, true);

        return label;
    }

    public Map<String, Object> getAsMap(DynamicPropertySet dps, Map<String, Object> presetValues)
    {
        Map<String, Object> values = getAsMap(dps);
        values.putAll(presetValues);

        return values;
    }

    public Map<String, Object> getAsMap(DynamicPropertySet dps)
    {
        Map<String, Object> values = new HashMap<>();
        dps.forEach(p -> values.put(p.getName(), p.getValue()));

        return values;
    }

    public Map<String, String> getAsMapStringValues(DynamicPropertySet dps)
    {
        Map<String, String> values = new HashMap<>();
        dps.forEach(p -> {
            if(p.getValue() != null && !p.getBooleanAttribute(BeanInfoConstants.LABEL_FIELD))
            {
                values.put(p.getName(), p.getValue().toString());
            }
        });

        return values;
    }

    public void setValueIfOneTag(DynamicPropertySet dps, List<String> propertyNames)
    {
        for (String name : propertyNames)
        {
            DynamicProperty property = dps.getProperty(name);
            Objects.requireNonNull(property);
            String[][] tags = (String[][]) property.getAttribute(BeanInfoConstants.TAG_LIST_ATTR);
            if(tags.length == 1)
            {
                property.setValue(tags[0][0]);
            }
        }
    }

    public <T extends DynamicPropertySet> T setOperationParams(T dps, Map<String, String> operationParams)
    {
        Map<String, ?> params = ParseRequestUtils.getOperationParamsWithoutFilter(operationParams);

        for (Map.Entry<String, ?> entry : params.entrySet())
        {
            DynamicProperty property = dps.getProperty(entry.getKey());
            if(property != null)
            {
                property.setValue(entry.getValue());
                property.setReadOnly(true);
            }
        }
        return dps;
    }
    
    private Entity getEntity(BeModelElement modelElements)
    {
        if(modelElements.getClass() == Entity.class)
        {
            return (Entity)modelElements;
        }
        else if(modelElements.getClass() == Query.class)
        {
            return ((Query)modelElements).getEntity();
        }
        else if(modelElements.getClass() == JavaOperation.class || modelElements.getClass() == GroovyOperation.class)
        {
            return ((Operation)modelElements).getEntity();
        }
        else
        {
            throw new RuntimeException("not supported modelElements");    
        }
    }
}
