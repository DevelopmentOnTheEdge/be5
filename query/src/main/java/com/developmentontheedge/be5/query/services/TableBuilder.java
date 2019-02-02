package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.config.CoreUtils;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryExecutor;
import com.developmentontheedge.be5.query.model.CellModel;
import com.developmentontheedge.be5.query.model.ColumnModel;
import com.developmentontheedge.be5.query.model.RawCellModel;
import com.developmentontheedge.be5.query.model.RowModel;
import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.query.util.TableUtils;
import com.developmentontheedge.be5.security.UserInfo;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.ID_COLUMN_LABEL;

public class TableBuilder
{
    private final Query query;
    private final UserInfo userInfo;
    private final QueryExecutor queryExecutor;
    private final CoreUtils coreUtils;

    @Inject
    TableBuilder(UserInfo userInfo, QueryExecutorFactory queryService, CoreUtils coreUtils,
                 @Assisted Query query, @Assisted Map<String, Object> parameters)
    {
        this.query = query;
        this.userInfo = userInfo;
        this.coreUtils = coreUtils;

        this.queryExecutor = queryService.get(query, parameters);
    }

    public interface TableBuilderFactory
    {
        TableBuilder create(Query query, Map<String, Object> parameters);
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

            if (columns.isEmpty())
            {
                columns.addAll(new PropertiesToRowTransformer(query.getEntity().getName(), query.getName(),
                        properties, userInfo, coreUtils).collectColumns());
            }
            rows.add(generateRow(query, properties));
        }
    }

    private RowModel generateRow(Query query, DynamicPropertySet properties)
            throws AssertionError
    {
        PropertiesToRowTransformer transformer = new PropertiesToRowTransformer(query.getEntity().getName(),
                query.getName(), properties, userInfo, coreUtils);
        List<RawCellModel> cells = transformer.collectCells(); // can contain hidden cells
        List<CellModel> processedCells = processCells(cells); // only visible cells

        String rowId = transformer.getRowId();
        if (query.getType() == QueryType.D1 && rowId == null)
        {
            throw Be5Exception.internal(ID_COLUMN_LABEL + " not found.");
        }
        return new RowModel(rowId, processedCells);
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
