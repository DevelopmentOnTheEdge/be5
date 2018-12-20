package com.developmentontheedge.be5.query.impl;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.CoreUtils;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryExecutor;
import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.be5.query.model.CellModel;
import com.developmentontheedge.be5.query.model.ColumnModel;
import com.developmentontheedge.be5.query.model.RawCellModel;
import com.developmentontheedge.be5.query.model.RowModel;
import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.query.services.QueryService;
import com.developmentontheedge.be5.query.util.DynamicPropertyMeta;
import com.developmentontheedge.be5.query.util.RoleFilter;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetAsMap;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.developmentontheedge.sql.format.ContextApplier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.ID_COLUMN_LABEL;
import static com.developmentontheedge.be5.query.impl.PropertiesToRowTransformer.shouldBeSkipped;

public class SqlTableBuilder
{
    private final Query query;
    private final UserInfo userInfo;
    private final Map<String, Object> parameters;
    private final QueryService queryService;
    private final QueryExecutor queryExecutor;
    private final UserAwareMeta userAwareMeta;
    private final CellFormatter cellFormatter;
    private final CoreUtils coreUtils;

    private ContextApplier contextApplier;

    public SqlTableBuilder(Query query, Map<String, Object> parameters, UserInfo userInfo, QueryService queryService,
                           UserAwareMeta userAwareMeta, Meta meta, CellFormatter cellFormatter, CoreUtils coreUtils,
                           QuerySession querySession)
    {
        this.query = query;
        this.parameters = parameters;
        this.userInfo = userInfo;

        this.queryService = queryService;
        this.cellFormatter = cellFormatter;
        this.coreUtils = coreUtils;

        Be5QueryContext context = new Be5QueryContext(query, parameters, querySession, userInfo, meta);
        this.contextApplier = new ContextApplier(context);
        this.queryExecutor = queryService.build(query, context);
        this.userAwareMeta = userAwareMeta;
    }

    public SqlTableBuilder offset(int offset)
    {
        this.queryExecutor.offset(offset);
        return this;
    }

    public SqlTableBuilder limit(int limit)
    {
        this.queryExecutor.limit(limit);
        return this;
    }

    public SqlTableBuilder sortOrder(int orderColumn, String orderDir)
    {
        queryExecutor.order(orderColumn, orderDir);
        return this;
    }

    public SqlTableBuilder selectable(boolean selectable)
    {
        queryExecutor.selectable(selectable);
        return this;
    }
//
//        public Builder setContextApplier(ContextApplier contextApplier)
//        {
//            queryExecutor.setContextApplier(contextApplier);
//            return this;
//        }

    public long count()
    {
        return queryExecutor.count();
    }

    public TableModel build()
    {
        List<DynamicPropertySet> propertiesList = queryExecutor.execute();

        boolean hasAggregate = false;
        if (StreamSupport.stream(propertiesList.get(0).spliterator(), false)
                .anyMatch(x -> DynamicPropertyMeta.get(x).containsKey(DatabaseConstants.COL_ATTR_AGGREGATE)))
        {
            hasAggregate = true;
            addAggregateRowIfNeeded(propertiesList, queryExecutor.executeAggregate());
        }

        List<ColumnModel> columns = new ArrayList<>();
        List<RowModel> rows = new ArrayList<>();

        collectColumnsAndRows(query.getEntity().getName(), query.getName(), propertiesList, columns, rows, ExecuteType.DEFAULT);

        Long totalNumberOfRows;
        if (queryExecutor.getOffset() + rows.size() < queryExecutor.getLimit())
        {
            totalNumberOfRows = (long) rows.size();
        }
        else
        {
            totalNumberOfRows = queryService.build(query, parameters).count();
        }

        return new TableModel(
                columns,
                rows,
                queryExecutor.isSelectable(),
                totalNumberOfRows,
                hasAggregate,
                queryExecutor.getOffset(),
                queryExecutor.getLimit(),
                queryExecutor.getOrderColumn(),
                queryExecutor.getOrderDir());
    }

    private void addAggregateRowIfNeeded(List<DynamicPropertySet> rows, List<DynamicPropertySet> aggregateRows)
    {
        DynamicPropertySet firstRow = aggregateRows.get(0);
        Map<String, Map<String, String>> aggregateColumnNames = getAggregateColumnNames(rows.get(0));
        Map<String, Double> aggregateValues = new HashMap<>();

        for (DynamicPropertySet row: aggregateRows)
        {
            for (Map.Entry<String, Map<String, String>> aggregateColumn: aggregateColumnNames.entrySet())
            {
                String name = aggregateColumn.getKey();
                Map<String, String> aggregate = aggregateColumn.getValue();
                Object value = row.getValue(aggregateColumn.getKey());
                if ("Number".equals(aggregate.get("type")))
                {
                    switch (aggregate.get("function"))
                    {
                        case "COUNT":
                            aggregateValues.put(name, aggregateValues.getOrDefault(name, 0.0) + 1);
                            break;
                        case "SUM":
                        case "AVG":
                            aggregateValues.put(name, aggregateValues.getOrDefault(name, 0.0) + getDoubleValue(value));
                            break;
                        default:
                            throw Be5Exception.internal("aggregate not support function: " + aggregate.get("function"));
                    }
                }
                else
                {
                    throw Be5Exception.internal("aggregate not support function: " + aggregate.get("function"));
                }
            }
        }
        for (Map.Entry<String, Map<String, String>> aggregateColumn: aggregateColumnNames.entrySet())
        {
            String name = aggregateColumn.getKey();
            Map<String, String> aggregate = aggregateColumn.getValue();
            if ("Number".equals(aggregate.get("type")))
            {
                switch (aggregate.get("function"))
                {
                    case "SUM":
                    case "COUNT":
                        break;
                    case "AVG":
                        aggregateValues.put(name, aggregateValues.get(name) / aggregateRows.size());
                        break;
                    default:
                        throw Be5Exception.internal("aggregate not support function: " + aggregate.get("function"));
                }
            }
            else
            {
                throw Be5Exception.internal("aggregate not support function: " + aggregate.get("function"));
            }
        }
        rows.add(getTotalRow(firstRow, aggregateValues));
    }

    private DynamicPropertySet getTotalRow(DynamicPropertySet firstRow, Map<String, Double> aggregateValues)
    {
        DynamicPropertySet res = new DynamicPropertySetSupport();
        boolean totalTitleAdded = false;
        for (Iterator<DynamicProperty> props = firstRow.propertyIterator(); props.hasNext();)
        {
            DynamicProperty prop = props.next();
            String name = prop.getName();
            DynamicProperty aggregateProp;

            if (aggregateValues.containsKey(name))
            {
                Map<String, String> aggregate = DynamicPropertyMeta.get(prop).get(DatabaseConstants.COL_ATTR_AGGREGATE);

                aggregateProp = new DynamicProperty(name, Double.class, aggregateValues.get(name));
                Map<String, Map<String, String>> options = new HashMap<>();
                options.put("css", Collections.singletonMap("class", aggregate.getOrDefault("cssClass", "")));
                options.put("format", Collections.singletonMap("mask", aggregate.getOrDefault("format", "")));
                DynamicPropertyMeta.set(aggregateProp, options);
            }
            else
            {
                aggregateProp = new DynamicProperty(name, prop.getType(), null);

                if (!totalTitleAdded && !shouldBeSkipped(prop))
                {
                    totalTitleAdded = true;
                    aggregateProp.setValue(userAwareMeta.getColumnTitle(query.getEntity().getName(), query.getName(),
                            "total"));
                    aggregateProp.setType(String.class);
                }
            }
            res.add(aggregateProp);
        }
        if (res.getProperty(ID_COLUMN_LABEL) != null) res.setValue(ID_COLUMN_LABEL, "aggregate");
        return res;
    }

    private Double getDoubleValue(Object value)
    {
        Double add;
        if (value instanceof List)
        {
            add = Double.parseDouble((String) ((List) ((List) value).get(0)).get(0));
        }
        else
        {
            if (value != null) add = Double.parseDouble("" + value);
            else add = 0.0;
        }
        return add;
    }

    private Map<String, Map<String, String>> getAggregateColumnNames(DynamicPropertySet firstRow)
    {
        Map<String, Map<String, String>> aggregateColumnNames = new HashMap<>();
        for (DynamicProperty dp : firstRow)
        {
            Map<String, Map<String, String>> meta = DynamicPropertyMeta.get(dp);
            Map<String, String> aggregateMeta = meta.get(DatabaseConstants.COL_ATTR_AGGREGATE);
            if (aggregateMeta != null) aggregateColumnNames.put(dp.getName(), aggregateMeta);
        }
        return aggregateColumnNames;
    }

    private void collectColumnsAndRows(String entityName, String queryName, List<DynamicPropertySet> list, List<ColumnModel> columns,
                                       List<RowModel> rows, ExecuteType executeType)
    {
        list.forEach(dps -> RoleFilter.filterBeanWithRoles(dps, userInfo.getCurrentRoles()));
        for (DynamicPropertySet properties : list)
        {
            if (columns.isEmpty())
            {
                columns.addAll(new PropertiesToRowTransformer(entityName, queryName, properties, userInfo, userAwareMeta, coreUtils).collectColumns());
            }
            rows.add(generateRow(entityName, queryName, properties, executeType));
        }
    }

    private RowModel generateRow(String entityName, String queryName, DynamicPropertySet properties, ExecuteType executeType) throws AssertionError
    {
        PropertiesToRowTransformer transformer = new PropertiesToRowTransformer(entityName, queryName, properties, userInfo, userAwareMeta, coreUtils);
        List<RawCellModel> cells = transformer.collectCells(); // can contain hidden cells
        addRowClass(cells);
        List<CellModel> processedCells = processCells(cells, executeType); // only visible cells

        String rowId = transformer.getRowId();
        if (queryExecutor.isSelectable() && rowId == null)
        {
            throw Be5Exception.internal(ID_COLUMN_LABEL + " not found.");
        }
        return new RowModel(rowId, processedCells);
    }

    private void addRowClass(List<RawCellModel> cells)
    {
        Optional<Object> addClassName = cells.stream()
                .filter(x -> x.name.equals(DatabaseConstants.CSS_ROW_CLASS) && x.content != null)
                .map(x -> x.content).findFirst();

        if (addClassName.isPresent())
        {
            for (RawCellModel cell : cells)
            {
                if (cell.options.get("grouping") != null) continue;
                Map<String, String> css = cell.options.putIfAbsent("css", new HashMap<>());
                if (css == null) css = cell.options.get("css");

                String className = css.getOrDefault("class", "");
                css.put("class", className + " " + addClassName.get());
            }
        }
    }

    /**
     * Processes each cell's content and selects only visible cells.
     *
     * @param cells raw cells
     *              columns.size() == cells.size()
     */
    private List<CellModel> processCells(List<RawCellModel> cells, ExecuteType executeType)
    {
        List<CellModel> processedCells = new ArrayList<>();
        DynamicPropertySet previousCells = new DynamicPropertySetAsMap();

        for (RawCellModel cell : cells)
        {
            Object processedContent;
            if (executeType == ExecuteType.DEFAULT)
                processedContent = cellFormatter.formatCell(cell, previousCells, query, contextApplier);
            else
                processedContent = cell.content;
            previousCells.add(new DynamicProperty(cell.name, processedContent == null ? String.class
                    : processedContent.getClass(), processedContent));
            if (!cell.hidden)
            {
                processedCells.add(new CellModel(processedContent, cell.options));
            }
        }

        return processedCells;
    }

}
