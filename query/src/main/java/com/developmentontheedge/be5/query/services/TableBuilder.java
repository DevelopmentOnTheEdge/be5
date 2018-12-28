package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.CoreUtils;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.util.LayoutUtils;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryConstants;
import com.developmentontheedge.be5.query.QueryExecutor;
import com.developmentontheedge.be5.query.model.CellModel;
import com.developmentontheedge.be5.query.model.ColumnModel;
import com.developmentontheedge.be5.query.model.RawCellModel;
import com.developmentontheedge.be5.query.model.RowModel;
import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.query.util.TableUtils;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.ID_COLUMN_LABEL;
import static com.developmentontheedge.be5.query.QueryConstants.LIMIT;
import static com.developmentontheedge.be5.query.QueryConstants.OFFSET;
import static com.developmentontheedge.be5.query.QueryConstants.ORDER_COLUMN;
import static com.developmentontheedge.be5.query.QueryConstants.ORDER_DIR;

public class TableBuilder
{
    private final Query query;
    private final UserInfo userInfo;
    private final QueryExecutor queryExecutor;
    private final UserAwareMeta userAwareMeta;
    private final CoreUtils coreUtils;

    @Inject
    TableBuilder(UserInfo userInfo, QueryExecutorFactory queryService, UserAwareMeta userAwareMeta,
                 CoreUtils coreUtils, @Assisted Query query, @Assisted Map<String, Object> parameters)
    {
        this.query = query;
        this.userInfo = userInfo;

        this.coreUtils = coreUtils;
        this.userAwareMeta = userAwareMeta;

        this.queryExecutor = queryService.get(query, parameters);
        config(query, parameters);
    }

    public interface TableModelFactory
    {
        TableBuilder create(Query query, Map<String, Object> parameters);
    }

    private void config(Query query, Map<String, Object> parameters)
    {
        int orderColumn = Integer.parseInt((String) parameters.getOrDefault(ORDER_COLUMN, "-1"));
        String orderDir = (String) parameters.getOrDefault(ORDER_DIR, "asc");
        int offset = Integer.parseInt((String) parameters.getOrDefault(OFFSET, "0"));
        int limit = Integer.parseInt((String) parameters.getOrDefault(LIMIT, Integer.toString(Integer.MAX_VALUE)));

        int maxLimit = userAwareMeta.getQuerySettings(query).getMaxRecordsPerPage();

        if (limit == Integer.MAX_VALUE)
        {
            limit = Integer.parseInt(LayoutUtils.getLayoutObject(query).getOrDefault("defaultPageLimit",
                    coreUtils.getSystemSetting("be5_defaultPageLimit", "10")).toString());
        }

        queryExecutor.order(orderColumn, orderDir);
        queryExecutor.offset(offset);
        queryExecutor.limit(Math.min(limit, maxLimit));
    }

    public TableModel get()
    {
        List<QRec> propertiesList;
        List<ColumnModel> columns = new ArrayList<>();
        List<RowModel> rows = new ArrayList<>();

        propertiesList = queryExecutor.execute();
        collectColumnsAndRows(query, propertiesList, columns, rows);

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

    private void collectColumnsAndRows(Query query, List<QRec> list,
                                       List<ColumnModel> columns, List<RowModel> rows)
    {
        for (DynamicPropertySet properties : list)
        {
            TableUtils.replaceBlob(properties);
            TableUtils.filterBeanWithRoles(properties, userInfo.getCurrentRoles());
            if (columns.isEmpty())
            {
                columns.addAll(new PropertiesToRowTransformer(query.getEntity().getName(), query.getName(),
                        properties, userInfo, userAwareMeta, coreUtils).collectColumns());
            }
            rows.add(generateRow(query, properties));
        }
    }

    private RowModel generateRow(Query query, DynamicPropertySet properties)
            throws AssertionError
    {
        PropertiesToRowTransformer transformer = new PropertiesToRowTransformer(query.getEntity().getName(),
                query.getName(), properties, userInfo, userAwareMeta, coreUtils);
        List<RawCellModel> cells = transformer.collectCells(); // can contain hidden cells
        addRowClass(cells);
        List<CellModel> processedCells = processCells(cells); // only visible cells

        String rowId = transformer.getRowId();
        if (query.getType() == QueryType.D1 && rowId == null)
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
            if (!cell.hidden)
            {
                processedCells.add(new CellModel(cell.content, cell.options));
            }
        }

        return processedCells;
    }
}
