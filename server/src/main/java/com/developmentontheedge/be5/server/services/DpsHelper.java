package com.developmentontheedge.be5.server.services;

import com.developmentontheedge.be5.databasemodel.util.DpsUtils;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.meta.UserAwareMeta;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.GroovyOperation;
import com.developmentontheedge.be5.metadata.model.JavaOperation;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.base.BeModelElement;
import com.developmentontheedge.be5.operation.validation.ValidationRules;
import com.developmentontheedge.be5.query.services.QueriesService;
import com.developmentontheedge.be5.util.FilterUtil;
import com.developmentontheedge.be5.util.JsonUtils;
import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.developmentontheedge.sql.format.MacroExpander;
import com.developmentontheedge.sql.model.AstBeParameterTag;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;
import com.google.common.math.LongMath;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.HIDDEN_COLUMN_PREFIX;
import static com.developmentontheedge.be5.metadata.DatabaseConstants.specialColumns;
import static com.developmentontheedge.be5.metadata.model.SqlColumnType.TYPE_BIGINT;
import static com.developmentontheedge.be5.metadata.model.SqlColumnType.TYPE_BOOL;
import static com.developmentontheedge.be5.metadata.model.SqlColumnType.TYPE_CHAR;
import static com.developmentontheedge.be5.metadata.model.SqlColumnType.TYPE_CURRENCY;
import static com.developmentontheedge.be5.metadata.model.SqlColumnType.TYPE_DECIMAL;
import static com.developmentontheedge.be5.metadata.model.SqlColumnType.TYPE_ENUM;
import static com.developmentontheedge.be5.metadata.model.SqlColumnType.TYPE_INT;
import static com.developmentontheedge.be5.metadata.model.SqlColumnType.TYPE_TEXT;
import static com.developmentontheedge.be5.metadata.model.SqlColumnType.TYPE_UBIGINT;
import static com.developmentontheedge.be5.metadata.model.SqlColumnType.TYPE_UINT;
import static com.developmentontheedge.be5.metadata.model.SqlColumnType.TYPE_VARCHAR;
import static com.developmentontheedge.be5.operation.validation.ValidationRules.range;
import static com.developmentontheedge.be5.operation.validation.ValidationRules.step;
import static com.google.common.collect.ImmutableMap.of;


public class DpsHelper
{
    private static final Logger log = Logger.getLogger(DpsHelper.class.getName());

    private final Meta meta;
    private final UserAwareMeta userAwareMeta;
    private final QueriesService queries;

    @Inject
    public DpsHelper(Meta meta, QueriesService queries, UserAwareMeta userAwareMeta)
    {
        this.meta = meta;
        this.userAwareMeta = userAwareMeta;
        this.queries = queries;
    }

//    public <T extends DynamicPropertySet> T addDp(T dps, BeModelElement modelElements, ResultSet resultSet,
// Map<String, Object> operationParams)
//    {
//        addDp(dps, modelElements, operationParams);
//        return setValues(dps, resultSet);
//    }

//    public <T extends DynamicPropertySet> T addDpWithoutTags(T dps, BeModelElement modelElements, ResultSet resultSet)
//    {
//        addDpWithoutTags(dps, modelElements);
//        return setValues(dps, resultSet);
//    }

//    public DynamicPropertySet getDpsForColumns(BeModelElement modelElements, Collection<String> columnNames,
// ResultSet resultSet)
//    {
//        DynamicPropertySet dps = getDpsForColumns(modelElements, columnNames);
//        return setValues(dps, resultSet);
//    }

//    public DynamicPropertySet getDpsExcludeAutoIncrement(BeModelElement modelElements, ResultSet resultSet)
//    {
//        DynamicPropertySet dps = getDpsExcludeAutoIncrement(modelElements);
//        return setValues(dps, resultSet);
//    }

    public <T extends DynamicPropertySet> T addDpExcludeAutoIncrement(T dps, BeModelElement modelElements,
                              Map<String, Object> operationParams, Map<String, ? super Object> values)
    {
        addDpExcludeAutoIncrement(dps, modelElements, operationParams);
        DpsUtils.setValues(dps, values);
        return setOperationParams(dps, operationParams);
    }

    public <T extends DynamicPropertySet> T addDpExcludeAutoIncrement(T dps, BeModelElement modelElements,
                              Map<String, Object> operationParams)
    {
        if (isHasOperationProperties(modelElements))
        {
            return addDpForColumns(dps, modelElements, getOperationProperties((Operation) modelElements),
                    operationParams);
        }

        List<String> excludedColumns = Collections.emptyList();
        ColumnDef column = meta.getColumn(getEntity(modelElements), getEntity(modelElements).getPrimaryKey());
        if (column != null && column.isAutoIncrement())
        {
            excludedColumns = Collections.singletonList(getEntity(modelElements).getPrimaryKey());
        }

        return addDpExcludeColumns(dps, modelElements, excludedColumns, operationParams);
    }

    private List<String> getOperationProperties(Operation modelElements)
    {
        Map<String, Object> layout = JsonUtils.getMapFromJson(modelElements.getLayout());
        String properties = (String) layout.get("properties");
        return Arrays.asList(properties.split(","));
    }

    private boolean isHasOperationProperties(BeModelElement modelElements)
    {
        if (!(modelElements instanceof Operation)) return false;
        Map<String, Object> layout = JsonUtils.getMapFromJson(((Operation) modelElements).getLayout());
        return layout.get("properties") != null;
    }

    public <T extends DynamicPropertySet> T addDp(T dps, BeModelElement modelElements,
                                                  Map<String, Object> operationParams)
    {
        return addDpExcludeColumns(dps, modelElements, Collections.emptyList(), operationParams);
    }

    public <T extends DynamicPropertySet> T addDpWithoutTags(T dps, BeModelElement modelElements)
    {
        return addDpsExcludedColumnsWithoutTags(dps, modelElements, Collections.emptyList());
    }

    public <T extends DynamicPropertySet> T addDpExcludeColumns(T dps, BeModelElement modelElements,
                    Collection<String> columnNames, Map<String, Object> operationParams,
                                                                Map<String, ? super Object> presetValues)
    {
        addDpExcludeColumns(dps, modelElements, columnNames, operationParams);

        DpsUtils.setValues(dps, presetValues);
        setOperationParams(dps, operationParams);

        return dps;
    }

    public <T extends DynamicPropertySet> T addDpExcludeColumns(T dps, BeModelElement modelElements,
                    Collection<String> columnNames, Map<String, Object> operationParams)
    {
        addDpsExcludedColumnsWithoutTags(dps, modelElements, columnNames);

        addTags(dps, modelElements, dps.asMap().keySet().stream()
                .filter(i -> !columnNames.contains(i))
                .collect(Collectors.toList()), operationParams);
        setOperationParams(dps, operationParams);

        return dps;
    }

    public <T extends DynamicPropertySet> T addTags(T dps, BeModelElement modelElements,
                                    Collection<String> columnNames, Map<String, Object> operationParams)
    {
        Map<String, ColumnDef> columns = getColumnsWithoutSpecial(modelElements);
        for (String propertyName : columnNames)
        {
            DynamicProperty property = dps.getProperty(propertyName);
            ColumnDef columnDef = columns.get(property.getName());
            if (columnDef != null) addTags(property, columnDef, operationParams);
        }
        return dps;
    }

    public <T extends DynamicPropertySet> T addDpsExcludedColumnsWithoutTags(T dps, BeModelElement modelElements,
                                                                             Collection<String> excludedColumns)
    {
        Map<String, ColumnDef> columns = getColumnsWithoutSpecial(modelElements);

        ArrayList<String> excludedColumnsList = new ArrayList<>(excludedColumns);
        for (Map.Entry<String, ColumnDef> entry : columns.entrySet())
        {
            if (!excludedColumnsList.contains(entry.getKey()))
            {
                DynamicProperty dynamicProperty = getDynamicProperty(entry.getValue());
                addMeta(dynamicProperty, entry.getValue(), modelElements);
                dps.add(dynamicProperty);
            }
            excludedColumnsList.remove(entry.getKey());
        }

        for (String propertyName : excludedColumnsList)
        {
            log.warning("Column " + propertyName + " not found in " + getEntity(modelElements));
        }
        return dps;
    }

    public <T extends DynamicPropertySet> T addDpForColumns(T dps, BeModelElement modelElements,
                        Collection<String> columnNames, Map<String, Object> operationParams,
                                                            Map<String, ? super Object> presetValues)
    {
        addDpForColumns(dps, modelElements, columnNames, operationParams);

        DpsUtils.setValues(dps, presetValues);
        setOperationParams(dps, operationParams);

        return dps;
    }

    public <T extends DynamicPropertySet> T addDpForColumns(T dps, BeModelElement modelElements,
                        Collection<String> columnNames, Map<String, Object> operationParams)
    {
        addDpForColumnsWithoutTags(dps, modelElements, columnNames);

        addTags(dps, modelElements, columnNames, operationParams);
        setOperationParams(dps, operationParams);

        return dps;
    }

    public <T extends DynamicPropertySet> T addDynamicProperties(T dps, BeModelElement modelElements,
                        Collection<String> propertyNames, Map<String, Object> operationParams)
    {
        Map<String, ColumnDef> columns = getColumnsWithoutSpecial(modelElements);

        for (String propertyName : propertyNames)
        {
            ColumnDef columnDef = columns.get(propertyName);
            DynamicProperty dynamicProperty = getDynamicProperty(columnDef);
            addMeta(dynamicProperty, columnDef, modelElements);
            addTags(dynamicProperty, columnDef, operationParams);

            dps.add(dynamicProperty);
        }
        return dps;
    }

    public <T extends DynamicPropertySet> T addDpForColumnsWithoutTags(T dps, BeModelElement modelElements,
                       Collection<String> columnNames, Map<String, ? super Object> presetValues)
    {
        addDpForColumnsWithoutTags(dps, modelElements, columnNames);

        addMeta(dps, modelElements);

        DpsUtils.setValues(dps, presetValues);

        return dps;
    }

    public <T extends DynamicPropertySet> T addDpForColumnsBase(T dps, BeModelElement modelElements,
                        Collection<String> columnNames, Map<String, ? super Object> presetValues)
    {
        addDpForColumnsBase(dps, modelElements, columnNames);

        DpsUtils.setValues(dps, presetValues);

        return dps;
    }

    public <T extends DynamicPropertySet> T addMeta(T dps, BeModelElement modelElements)
    {
        Map<String, ColumnDef> columns = getColumnsWithoutSpecial(modelElements);
        for (DynamicProperty property : dps)
        {
            ColumnDef columnDef = columns.get(property.getName());
            if (columnDef != null) addMeta(property, columnDef, modelElements);
        }
        return dps;
    }

    public <T extends DynamicPropertySet> T addDpForColumnsWithoutTags(T dps, BeModelElement modelElements,
                       Collection<String> columnNames)
    {
        Map<String, ColumnDef> columns = getColumnsWithoutSpecial(modelElements);
        for (String propertyName : columnNames)
        {
            ColumnDef columnDef = columns.get(propertyName);
            if (columnDef != null)
            {
                DynamicProperty dynamicProperty = getDynamicProperty(columnDef);
                addMeta(dynamicProperty, columnDef, modelElements);
                dps.add(dynamicProperty);
            }
            else
            {
                throw Be5Exception.internal(getEntity(modelElements) + "' not contain column " + propertyName);
            }
        }
        return dps;
    }

    public DynamicProperty getDynamicProperty(ColumnDef columnDef)
    {
        DynamicProperty prop = new DynamicProperty(columnDef.getName(), meta.getColumnType(columnDef));

        String label = DynamicPropertySetSupport.makeBetterDisplayName( prop.getName() );

        if( label.length() > 0 )
        {
            prop.setDisplayName( label );
        }
         
        return prop;   
    }

    public <T extends DynamicPropertySet> T addDpBase(T dps, BeModelElement modelElements)
    {
        Map<String, ColumnDef> columns = getColumnsWithoutSpecial(modelElements);
        for (Map.Entry<String, ColumnDef> column : columns.entrySet())
        {
            dps.add(getDynamicProperty(column.getValue()));
        }
        return dps;
    }

    public <T extends DynamicPropertySet> T addDpForColumnsBase(T dps, BeModelElement modelElements,
                                    Collection<String> columnNames)
    {
        Map<String, ColumnDef> columns = getColumnsWithoutSpecial(modelElements);
        for (String propertyName : columnNames)
        {
            ColumnDef columnDef = columns.get(propertyName);
            if (columnDef != null)
            {
                DynamicProperty dynamicProperty = getDynamicProperty(columnDef);
                dps.add(dynamicProperty);
            }
            else
            {
                throw Be5Exception.internal(getEntity(modelElements) + "' not contain column " + propertyName);
            }
        }
        return dps;
    }

    public <T extends DynamicPropertySet> T addParamsFromQuery(T dps, BeModelElement modelElements,
                                                               Query query, Map<String, Object> operationParams)
    {
        AstStart ast;
        try
        {
            ast = SqlQuery.parse(query.getFinalQuery());
            new MacroExpander().expandMacros(ast);
        }
        catch (RuntimeException e)
        {
            log.log(Level.SEVERE, "SqlQuery.parse error: ", e);
            throw Be5Exception.internalInQuery(query, e);
        }

        List<String> usedParams = ast.tree().select(AstBeParameterTag.class).map(AstBeParameterTag::getName).toList();
        if (isHasOperationProperties(modelElements))
        {
            List<String> operationProperties = getOperationProperties((Operation) modelElements);
            usedParams.removeIf(name -> !operationProperties.contains(name));
        }

        Map<String, ColumnDef> entityColumns = meta.getColumns(getEntity(modelElements));
        for (String param: usedParams)
        {
            if (entityColumns.containsKey(param))
            {
                addDpForColumns(dps, modelElements, Collections.singletonList(param), operationParams);
            }
            else
            {
                DynamicProperty prop = new DynamicProperty(param, String.class);

                String label = DynamicPropertySetSupport.makeBetterDisplayName( prop.getName() );

                if( label.length() > 0 )
                {
                    prop.setDisplayName( label );
                }

                dps.add( prop );
            }
        }

        return dps;
    }

    public DynamicProperty addMeta(DynamicProperty dp, ColumnDef columnDef, BeModelElement modelElements)
    {
        String colName = columnDef.getName();
        if (modelElements.getClass() == Query.class)
        {
            String displayName = userAwareMeta.getColumnTitle(
                    columnDef.getEntity().getName(), modelElements.getName(), colName );
            if( displayName != null && !displayName.equals( colName ) )
            {
                dp.setDisplayName( displayName );
            }
        }
        else if (modelElements.getClass() == Entity.class)
        {
            String displayName = userAwareMeta.getColumnTitle(
                    columnDef.getEntity().getName(), colName );
            if( displayName != null && !displayName.equals( colName ) )
            {
                dp.setDisplayName( displayName );
            }
        }
        else if (modelElements.getClass() == JavaOperation.class || modelElements.getClass() == GroovyOperation.class)
        {
            System.err.println( "colName = " + colName );  
            System.err.println( "columnDef.getEntity().getName() = " + columnDef.getEntity().getName() );  
            System.err.println( "modelElements.getName() = " + modelElements.getName() );  
            String displayName = userAwareMeta.getLocalizedOperationField(
                    columnDef.getEntity().getName(), modelElements.getName(), colName );
            System.err.println( "displayName = " + displayName );  
            if( displayName != null && !displayName.equals( colName ) )
            {
                dp.setDisplayName( displayName );
            }
        }

        if (columnDef.getDefaultValue() != null)
        {
            dp.setValue(meta.getColumnDefaultValue(columnDef));
        }

        if (columnDef.getPlaceholder() != null)
        {
            dp.setAttribute(BeanInfoConstants.PLACEHOLDER, meta.getPlaceholder(columnDef));
        }

        if (columnDef.isCanBeNull() ||
                (columnDef.getTypeString().equals(TYPE_BOOL) && columnDef.getDefaultValue() != null))
        {
            dp.setCanBeNull(true);
        }

        String typeName = columnDef.getType().getTypeName();

        if (TYPE_TEXT.equals(typeName))
        {
            dp.setAttribute(BeanInfoConstants.EXTRA_ATTRS, new String[][]{{BeanInfoConstants.PROPERTY_INPUT_TYPE, "textArea"}});
        }

        if (TYPE_VARCHAR.equals(typeName) || TYPE_CHAR.equals(typeName))
        {
            dp.setAttribute(BeanInfoConstants.COLUMN_SIZE_ATTR, columnDef.getType().getSize());
        }

        if (TYPE_DECIMAL.equals(typeName))
        {
            int size = columnDef.getType().getSize();
            dp.setAttribute(BeanInfoConstants.VALIDATION_RULES, Arrays.asList(
                    getRange(size, false),
                    step(getPrecision(columnDef.getType().getPrecision()))
            ));
        }

        if (TYPE_CURRENCY.equals(typeName))
        {
            dp.setAttribute(BeanInfoConstants.VALIDATION_RULES, Arrays.asList(
                    getRange(columnDef.getType().getSize(), false),
                    step(0.01)
            ));
        }

        if (TYPE_INT.equals(typeName) || TYPE_UINT.equals(typeName))
        {
            boolean unsigned = TYPE_UINT.equals(typeName);

            dp.setAttribute(BeanInfoConstants.VALIDATION_RULES, Arrays.asList(
                    range(unsigned ? 0 : Integer.MIN_VALUE, Integer.MAX_VALUE),
                    step(1)
            ));
        }

        if (TYPE_BIGINT.equals(typeName) || TYPE_UBIGINT.equals(typeName))
        {
            boolean unsigned = TYPE_UBIGINT.equals(typeName);

            dp.setAttribute(BeanInfoConstants.VALIDATION_RULES, Arrays.asList(
                    range(unsigned ? 0 : Long.MIN_VALUE, Long.MAX_VALUE),
                    step(1)
            ));
        }

        if (columnDef.getName().endsWith(HIDDEN_COLUMN_PREFIX)) dp.setHidden(true);

        return dp;
    }

    public String getPrecision(int precision)
    {
        switch (precision)
        {
            case 0:
                return "1";
            case 1:
                return "0.1";
            case 2:
                return "0.01";
            case 3:
                return "0.001";
            case 4:
                return "1.0E-4";
            default:
                return "1.0E-" + Integer.toString(precision);
        }
    }

    public ValidationRules.Rule getRange(int size, boolean unsigned)
    {
        if (size <= 18)
        {
            return range(unsigned ? 0 : -LongMath.pow(10, size), LongMath.pow(10, size));
        }
        else
        {
            return range(unsigned ? 0 : -Math.pow(10, size), Math.pow(10, size));
        }
    }

    public void addTags(DynamicProperty dp, ColumnDef columnDef, Map<String, Object> operationParams)
    {
        String[][] tags = getTags(columnDef, operationParams);

        if (tags != null)
        {
            dp.setAttribute(BeanInfoConstants.TAG_LIST_ATTR, tags);
            if (tags.length == 1 && !dp.isCanBeNull())
            {
                dp.setValue(tags[0][0]);
            }
        }
    }

    public String[][] getTags(ColumnDef columnDef, Map<String, Object> operationParams)
    {
        String viewName = columnDef.getViewName();
        Map<String, Object> contextParams = FilterUtil.getContextParams(operationParams);

        if (columnDef.getType().getTypeName().equals(TYPE_BOOL) || columnDef.getType().getTypeName().equals(TYPE_ENUM))
        {
            String[][] tags;
            if (columnDef.getType().getTypeName().equals(TYPE_BOOL))
            {
                tags = queries.getTagsYesNo();
            }
            else
            {
                tags = queries.getTagsFromEnum(columnDef);
            }
            if (contextParams.containsKey(columnDef.getName()))
            {
                return getOneTag(tags, contextParams.get(columnDef.getName()));
            }
            else
            {
                return tags;
            }
        }
        else
        {
            String tableName = columnDef.getTableTo();
            if (tableName != null && meta.getEntity(tableName) != null)
            {
                Map<String, Object> tagsParams;
                if (contextParams.containsKey(columnDef.getName()))
                {
                    Object value = contextParams.get(columnDef.getName());
                    tagsParams = of(meta.getEntity(tableName).getPrimaryKey(), value);
                }
                else
                {
                    tagsParams = withEntityPrefix(columnDef.getEntity(), contextParams);
                }
                return queries.getTagsFromCustomSelectionView(tableName, viewName, tagsParams);
            }
        }
        return null;
    }

    private String[][] getOneTag(String[][] tags, Object value)
    {
        for (String[] tag : tags)
        {
            if (tag[0].equals(value)) return new String[][]{tag};
        }
        return new String[][]{};
    }

    private Map<String, Object> withEntityPrefix(Entity entity, Map<String, Object> operationParams)
    {
        Map<String, ColumnDef> columns = meta.getColumns(entity);
        return operationParams.entrySet().stream()
                .collect(Collectors.toMap(e -> {
                    if (columns.containsKey(e.getKey()))
                    {
                        return entity.getName() + "." + e.getKey();
                    }
                    else
                    {
                        return e.getKey();
                    }
                }, Map.Entry::getValue));
    }

    public Object[] getValues(DynamicPropertySet dps)
    {
        return StreamSupport.stream(dps.spliterator(), false)
                .map(DynamicProperty::getValue).toArray();
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
        label.setDisplayName("");
        label.setAttribute(BeanInfoConstants.LABEL_FIELD, true);
        label.setAttribute(BeanInfoConstants.CAN_BE_NULL, true);
        label.setReadOnly(true);
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
        Map<String, Object> values = new LinkedHashMap<>();
        dps.forEach(p -> values.put(p.getName(), p.getValue()));

        return values;
    }

    public Map<String, Object> getAsMapStringValues(DynamicPropertySet dps)
    {
        Map<String, Object> values = new LinkedHashMap<>();
        dps.forEach(p -> {
            if (p.getValue() != null && !p.getBooleanAttribute(BeanInfoConstants.LABEL_FIELD))
            {
                if (p.getValue() instanceof Object[])
                {
                    values.put(p.getName(), Arrays.stream((Object[]) p.getValue()).map(x -> x + "")
                            .toArray(String[]::new));
                }
                else
                {
                    values.put(p.getName(), p.getValue().toString());
                }
            }
        });

        return values;
    }

    public <T extends DynamicPropertySet> T setOperationParams(T dps, Map<String, Object> operationParams)
    {
        Map<String, ?> contextParams = FilterUtil.getContextParams(operationParams);

        for (Map.Entry<String, ?> entry : contextParams.entrySet())
        {
            DynamicProperty property = dps.getProperty(entry.getKey());
            if (property != null)
            {
                property.setValue(entry.getValue());
                property.setReadOnly(true);
            }
        }
        return dps;
    }

    private Entity getEntity(BeModelElement modelElements)
    {
        if (modelElements.getClass() == Entity.class)
        {
            return (Entity) modelElements;
        }
        else if (modelElements.getClass() == Query.class)
        {
            return ((Query) modelElements).getEntity();
        }
        else if (modelElements.getClass() == JavaOperation.class || modelElements.getClass() == GroovyOperation.class)
        {
            return ((Operation) modelElements).getEntity();
        }
        else
        {
            throw new RuntimeException("not supported modelElements");
        }
    }

    protected Map<String, ColumnDef> getColumnsWithoutSpecial(BeModelElement modelElements)
    {
        Map<String, ColumnDef> columns = meta.getColumns(getEntity(modelElements));
        for (String specialColumnsName : specialColumns)
        {
            columns.remove(specialColumnsName);
        }
        return columns;
    }

    public DynamicPropertySet filterEntityParams(Entity entity, DynamicPropertySet parameters)
    {
        DynamicPropertySet result = new DynamicPropertySetSupport();
        Map<String, ColumnDef> columns = meta.getColumns(entity);
        for (DynamicProperty property : parameters)
        {
            if (columns.containsKey(property.getName()))
            {
                result.add(property);
            }
        }
        return result;
    }

}
