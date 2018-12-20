package com.developmentontheedge.be5.query.impl;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.CoreUtils;
import com.developmentontheedge.be5.base.services.GroovyRegister;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.OrderedQueryExecutor;
import com.developmentontheedge.be5.query.QueryConstants;
import com.developmentontheedge.be5.query.QueryExecutor;
import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.be5.query.model.CellModel;
import com.developmentontheedge.be5.query.model.ColumnModel;
import com.developmentontheedge.be5.query.model.RawCellModel;
import com.developmentontheedge.be5.query.model.RowModel;
import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.query.services.QueryExecutorFactory;
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
    private final OrderedQueryExecutor queryExecutor;
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
        this.userAwareMeta = userAwareMeta;
        this.injector = injector;

        Be5QueryContext context = new Be5QueryContext(query, parameters, querySession, userInfo, meta);
        this.contextApplier = new ContextApplier(context);
        if (query.getType() == D1 || query.getType() == D1_UNKNOWN)
            this.queryExecutor = queryService.build(query, context);
        else
            this.queryExecutor = getQueryBuilder(query, parameters);
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

    public TableModel build()
    {
        List<DynamicPropertySet> propertiesList;
        List<ColumnModel> columns = new ArrayList<>();
        List<RowModel> rows = new ArrayList<>();
        Long totalNumberOfRows;

        if (query.getType() == D1 || query.getType() == D1_UNKNOWN)
        {
            propertiesList = queryExecutor.execute();

            collectColumnsAndRows(query.getEntity().getName(), query.getName(), propertiesList, columns, rows);

            if (queryExecutor.getOffset() + rows.size() < queryExecutor.getLimit())
            {
                totalNumberOfRows = (long) rows.size();
            }
            else
            {
                totalNumberOfRows = queryService.build(query, parameters).count();
            }
        }
        else if (query.getType() == JAVA || query.getType() == GROOVY)
        {
            propertiesList = queryExecutor.execute();
            collectColumnsAndRows(query.getEntity().getName(), query.getName(), propertiesList, columns, rows);
            totalNumberOfRows = (long) rows.size();
        }
        else
        {
            throw Be5Exception.internal("Unknown action type '" + query.getType() + "'");
        }

        return new TableModel(
                columns,
                rows,
                queryExecutor.isSelectable(),
                totalNumberOfRows,
                queryExecutor.getOffset(),
                queryExecutor.getLimit(),
                queryExecutor.getOrderColumn(),
                queryExecutor.getOrderDir());
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
                .filter(x -> x.name.equals(QueryConstants.CSS_ROW_CLASS) && x.content != null)
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

    private QueryExecutor getQueryBuilder(Query query, Map<String, Object> parameters)
    {
        QueryExecutor tableBuilder;

        switch (query.getType())
        {
            case JAVA:
                try
                {
                    tableBuilder = (QueryExecutor) Class.forName(query.getQuery()).newInstance();
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
                        tableBuilder = (QueryExecutor) aClass.newInstance();
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
