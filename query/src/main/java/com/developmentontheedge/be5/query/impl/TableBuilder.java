package com.developmentontheedge.be5.query.impl;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.CoreUtils;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryConstants;
import com.developmentontheedge.be5.query.QueryExecutor;
import com.developmentontheedge.be5.query.model.CellModel;
import com.developmentontheedge.be5.query.model.ColumnModel;
import com.developmentontheedge.be5.query.model.RawCellModel;
import com.developmentontheedge.be5.query.model.RowModel;
import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.query.services.QueryExecutorFactory;
import com.developmentontheedge.be5.query.util.TableUtils;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.ID_COLUMN_LABEL;

public class TableBuilder
{
    private final Query query;
    private final UserInfo userInfo;
    private final QueryExecutor queryExecutor;
    private final UserAwareMeta userAwareMeta;
    private final CoreUtils coreUtils;

    public TableBuilder(Query query, Map<String, Object> parameters, UserInfo userInfo,
                        QueryExecutorFactory queryService, UserAwareMeta userAwareMeta, CoreUtils coreUtils)
    {
        this.query = query;
        this.userInfo = userInfo;

        this.coreUtils = coreUtils;
        this.userAwareMeta = userAwareMeta;

        this.queryExecutor = queryService.get(query, parameters);
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

    public TableModel get()
    {
        List<DynamicPropertySet> propertiesList;
        List<ColumnModel> columns = new ArrayList<>();
        List<RowModel> rows = new ArrayList<>();

        propertiesList = queryExecutor.execute();
        collectColumnsAndRows(query.getEntity().getName(), query.getName(), propertiesList, columns, rows);

        return new TableModel(
                columns,
                rows,
                queryExecutor.isSelectable(),
                getCount(rows),
                queryExecutor.getOffset(),
                queryExecutor.getLimit(),
                queryExecutor.getOrderColumn(),
                queryExecutor.getOrderDir());
    }

    private Long getCount(List<RowModel> rows)
    {
        Long totalNumberOfRows;
        if (queryExecutor.getOffset() + rows.size() < queryExecutor.getLimit())
        {
            totalNumberOfRows = (long) rows.size();
        }
        else
        {
            totalNumberOfRows = queryExecutor.count();
        }
        return totalNumberOfRows;
    }

    private void collectColumnsAndRows(String entityName, String queryName, List<DynamicPropertySet> list,
                                       List<ColumnModel> columns, List<RowModel> rows)
    {
        list.forEach(dps -> TableUtils.filterBeanWithRoles(dps, userInfo.getCurrentRoles()));
        for (DynamicPropertySet properties : list)
        {
            if (columns.isEmpty())
            {
                columns.addAll(new PropertiesToRowTransformer(entityName, queryName, properties, userInfo,
                        userAwareMeta, coreUtils).collectColumns());
            }
            rows.add(generateRow(entityName, queryName, properties));
        }
    }

    private RowModel generateRow(String entityName, String queryName, DynamicPropertySet properties)
            throws AssertionError
    {
        PropertiesToRowTransformer transformer = new PropertiesToRowTransformer(entityName, queryName, properties,
                userInfo, userAwareMeta, coreUtils);
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

        for (RawCellModel cell : cells)
        {
//            Object processedContent = cellFormatter.formatCell(cell, previousCells, query, contextApplier);
//            previousCells.add(new DynamicProperty(cell.name, processedContent == null ? String.class
//                    : processedContent.getClass(), processedContent));
            if (!cell.hidden)
            {
                processedCells.add(new CellModel(cell.content, cell.options));
            }
        }

        return processedCells;
    }
}
