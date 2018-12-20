package com.developmentontheedge.be5.query.impl;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.CoreUtils;
import com.developmentontheedge.be5.base.services.GroovyRegister;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.DpsTableBuilder;
import com.developmentontheedge.be5.query.QueryExecutor;
import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.be5.query.model.CellModel;
import com.developmentontheedge.be5.query.model.ColumnModel;
import com.developmentontheedge.be5.query.model.RawCellModel;
import com.developmentontheedge.be5.query.model.RowModel;
import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.query.services.QueryExecutorFactory;
import com.developmentontheedge.be5.query.util.DynamicPropertyMeta;
import com.developmentontheedge.be5.query.util.TableUtils;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetAsMap;
import com.developmentontheedge.sql.format.ContextApplier;
import com.google.inject.Injector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.ID_COLUMN_LABEL;
import static com.developmentontheedge.be5.metadata.QueryType.D1;
import static com.developmentontheedge.be5.metadata.QueryType.D1_UNKNOWN;
import static com.developmentontheedge.be5.metadata.QueryType.GROOVY;
import static com.developmentontheedge.be5.metadata.QueryType.JAVA;

public class TableBuilder
{
    private final Query query;
    private final UserInfo userInfo;
    private final Map<String, Object> parameters;
    private final QueryExecutorFactory queryService;
    private final QueryExecutor queryExecutor;
    private final UserAwareMeta userAwareMeta;
    private final CellFormatter cellFormatter;
    private final CoreUtils coreUtils;
    private final GroovyRegister groovyRegister;
    private final Injector injector;

    private ContextApplier contextApplier;

    public TableBuilder(Query query, Map<String, Object> parameters, UserInfo userInfo, QueryExecutorFactory queryService,
                        UserAwareMeta userAwareMeta, Meta meta, CellFormatter cellFormatter, CoreUtils coreUtils,
                        QuerySession querySession, GroovyRegister groovyRegister, Injector injector)
    {
        this.query = query;
        this.parameters = parameters;
        this.userInfo = userInfo;

        this.queryService = queryService;
        this.cellFormatter = cellFormatter;
        this.coreUtils = coreUtils;
        this.groovyRegister = groovyRegister;
        this.injector = injector;

        Be5QueryContext context = new Be5QueryContext(query, parameters, querySession, userInfo, meta);
        this.contextApplier = new ContextApplier(context);
        this.queryExecutor = queryService.build(query, context);
        this.userAwareMeta = userAwareMeta;
    }

    public TableBuilder offset(int offset)
    {
        this.queryExecutor.offset(offset);
        return this;
    }

    public TableBuilder limit(int limit)
    {
        this.queryExecutor.limit(limit);
        return this;
    }

    public TableBuilder sortOrder(int orderColumn, String orderDir)
    {
        queryExecutor.order(orderColumn, orderDir);
        return this;
    }

    public TableBuilder selectable(boolean selectable)
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
        List<DynamicPropertySet> propertiesList;
        boolean hasAggregate;
        List<ColumnModel> columns = new ArrayList<>();
        List<RowModel> rows = new ArrayList<>();

        if(query.getType() == D1 || query.getType() == D1_UNKNOWN)
        {
            propertiesList = queryExecutor.execute();
            hasAggregate = addAggregateRowIfNeeded(propertiesList);

            collectColumnsAndRows(query.getEntity().getName(), query.getName(), propertiesList, columns, rows);

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
        else if (query.getType() == JAVA || query.getType() == GROOVY)
        {
            DpsTableBuilder tableBuilder = getFromTableBuilder(query, parameters);
            propertiesList = tableBuilder.getTableModel();
            collectColumnsAndRows(query.getEntity().getName(), query.getName(), propertiesList, columns, rows);
            return new TableModel(
                    columns,
                    rows,
                    queryExecutor.isSelectable(),
                    (long) rows.size(),
                    false,
                    queryExecutor.getOffset(),
                    queryExecutor.getLimit(),
                    queryExecutor.getOrderColumn(),
                    queryExecutor.getOrderDir());
        }
        else
        {
            throw Be5Exception.internal("Unknown action type '" + query.getType() + "'");
        }
    }

    private boolean addAggregateRowIfNeeded(List<DynamicPropertySet> propertiesList)
    {
        if (propertiesList.size() > 0 && StreamSupport.stream(propertiesList.get(0).spliterator(), false)
                .anyMatch(x -> DynamicPropertyMeta.get(x).containsKey(DatabaseConstants.COL_ATTR_AGGREGATE)))
        {
            String totalTitle = userAwareMeta.getColumnTitle(query.getEntity().getName(), query.getName(), "total");
            TableUtils.addAggregateRowIfNeeded(propertiesList, queryExecutor.executeAggregate(), totalTitle);
            return true;
        }
        return false;
    }

    private void collectColumnsAndRows(String entityName, String queryName, List<DynamicPropertySet> list, List<ColumnModel> columns,
                                       List<RowModel> rows)
    {
        list.forEach(dps -> TableUtils.filterBeanWithRoles(dps, userInfo.getCurrentRoles()));
        for (DynamicPropertySet properties : list)
        {
            if (columns.isEmpty())
            {
                columns.addAll(new PropertiesToRowTransformer(entityName, queryName, properties, userInfo, userAwareMeta, coreUtils).collectColumns());
            }
            rows.add(generateRow(entityName, queryName, properties));
        }
    }

    private RowModel generateRow(String entityName, String queryName, DynamicPropertySet properties) throws AssertionError
    {
        PropertiesToRowTransformer transformer = new PropertiesToRowTransformer(entityName, queryName, properties, userInfo, userAwareMeta, coreUtils);
        List<RawCellModel> cells = transformer.collectCells(); // can contain hidden cells
        addRowClass(cells);
        List<CellModel> processedCells = processCells(cells); // only visible cells

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
    private List<CellModel> processCells(List<RawCellModel> cells)
    {
        List<CellModel> processedCells = new ArrayList<>();
        DynamicPropertySet previousCells = new DynamicPropertySetAsMap();

        for (RawCellModel cell : cells)
        {
            Object processedContent = cellFormatter.formatCell(cell, previousCells, query, contextApplier);
            previousCells.add(new DynamicProperty(cell.name, processedContent == null ? String.class
                    : processedContent.getClass(), processedContent));
            if (!cell.hidden)
            {
                processedCells.add(new CellModel(processedContent, cell.options));
            }
        }

        return processedCells;
    }

    private DpsTableBuilder getFromTableBuilder(Query query, Map<String, Object> parameters)
    {
        DpsTableBuilder tableBuilder;

        switch (query.getType())
        {
            case JAVA:
                try
                {
                    tableBuilder = (DpsTableBuilder) Class.forName(query.getQuery()).newInstance();
                    break;
                }
                catch (ClassNotFoundException | IllegalAccessException | InstantiationException e)
                {
                    throw Be5Exception.internalInQuery(query, e);
                }
            case GROOVY:
                try
                {
                    Class aClass = groovyRegister.getClass(query.getEntity() + query.getName(),
                            query.getQuery(), query.getFileName());

                    if (aClass != null)
                    {
                        tableBuilder = (DpsTableBuilder) aClass.newInstance();
                        break;
                    }
                    else
                    {
                        throw Be5Exception.internal("Class " + query.getQuery() + " is null.");
                    }
                }
                catch (NoClassDefFoundError | IllegalAccessException | InstantiationException e)
                {
                    throw new UnsupportedOperationException("Groovy feature has been excluded", e);
                }
            default:
                throw Be5Exception.internal("Not support operation type: " + query.getType());
        }

        injector.injectMembers(tableBuilder);
        tableBuilder.initialize(query, parameters);

        return tableBuilder;
    }
}
