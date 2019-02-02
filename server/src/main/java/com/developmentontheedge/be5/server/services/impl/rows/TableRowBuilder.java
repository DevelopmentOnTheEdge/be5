package com.developmentontheedge.be5.server.services.impl.rows;

import com.developmentontheedge.be5.config.CoreUtils;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.query.util.TableUtils;
import com.developmentontheedge.be5.security.UserInfoProvider;
import com.developmentontheedge.be5.server.model.table.CellModel;
import com.developmentontheedge.be5.server.model.table.ColumnModel;
import com.developmentontheedge.be5.server.model.table.RawCellModel;
import com.developmentontheedge.be5.server.model.table.RowModel;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.ID_COLUMN_LABEL;

public class TableRowBuilder
{
    private final UserInfoProvider userInfoProvider;
    private final CoreUtils coreUtils;

    @Inject
    TableRowBuilder(UserInfoProvider userInfoProvider, CoreUtils coreUtils)
    {
        this.userInfoProvider = userInfoProvider;
        this.coreUtils = coreUtils;
    }

    public List<RowModel> collectRows(Query query, List<QRec> list)
    {
        ArrayList<RowModel> rows = new ArrayList<>();
        for (DynamicPropertySet properties : list)
        {
            TableUtils.replaceBlob(properties);
            rows.add(generateRow(query, properties));
        }
        return rows;
    }

    public List<ColumnModel> collectColumns(Query query, List<QRec> list)
    {
        if (list.size() > 0)
        {
            return new PropertiesToRowTransformer(query.getEntity().getName(), query.getName(),
                    list.get(0), userInfoProvider.getLoggedUser(), coreUtils).collectColumns();
        }
        else
        {
            return Collections.emptyList();
        }
    }

    private RowModel generateRow(Query query, DynamicPropertySet properties)
            throws AssertionError
    {
        PropertiesToRowTransformer transformer = new PropertiesToRowTransformer(query.getEntity().getName(),
                query.getName(), properties, userInfoProvider.getLoggedUser(), coreUtils);
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
