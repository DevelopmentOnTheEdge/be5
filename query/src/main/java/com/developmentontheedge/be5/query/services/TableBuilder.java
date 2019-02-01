package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.config.CoreUtils;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.meta.UserAwareMeta;
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
import com.developmentontheedge.be5.security.UserInfo;
import com.developmentontheedge.be5.util.JsonUtils;
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

        this.queryExecutor = queryService.get(query, updateLimit(query, parameters));
    }

    public interface TableBuilderFactory
    {
        TableBuilder create(Query query, Map<String, Object> parameters);
    }

    private Map<String, Object> updateLimit(Query query, Map<String, Object> parameters)
    {
        HashMap<String, Object> newParams = new HashMap<>(parameters);
        int limit = Integer.parseInt((String) newParams.getOrDefault(LIMIT, Integer.toString(Integer.MAX_VALUE)));

        int maxLimit = userAwareMeta.getQuerySettings(query).getMaxRecordsPerPage();

        if (limit == Integer.MAX_VALUE)
        {
            Map<String, Object> layout = JsonUtils.getMapFromJson(query.getLayout());
            limit = Integer.parseInt(layout.getOrDefault("defaultPageLimit",
                    coreUtils.getSystemSetting("be5_defaultPageLimit", "10")).toString());
        }
        newParams.put(LIMIT, Math.min(limit, maxLimit) + "");
        return newParams;
    }

    public TableModel get()
    {
        List<ColumnModel> columns = new ArrayList<>();
        List<RowModel> rows = new ArrayList<>();

        collectColumnsAndRows(query, getRows(), columns, rows);

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

    private List<QRec> getRows()
    {
        try
        {
            return queryExecutor.execute();
        }
        catch (RuntimeException e)
        {
            throw Be5Exception.internalInQuery(query, e);
        }
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
            try
            {
                totalNumberOfRows = queryExecutor.count();
            }
            catch (RuntimeException e)
            {
                throw Be5Exception.internalInQuery(query, e);
            }
        }
        return totalNumberOfRows;
    }

    private void collectColumnsAndRows(Query query, List<QRec> list,
                                       List<ColumnModel> columns, List<RowModel> rows)
    {
        for (DynamicPropertySet properties : list)
        {
            TableUtils.replaceBlob(properties);

            //TODO move to Be5SqlQueryExecutor
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

    //TODO move to Be5SqlQueryExecutor
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
                processedCells.add(new CellModel(cell.name, cell.title, cell.content, cell.options));
            }
        }

        return processedCells;
    }
}
