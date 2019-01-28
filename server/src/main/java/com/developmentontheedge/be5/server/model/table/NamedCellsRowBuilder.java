package com.developmentontheedge.be5.server.model.table;

import com.developmentontheedge.be5.query.model.CellModel;
import com.developmentontheedge.be5.query.model.RowModel;
import com.developmentontheedge.be5.query.model.TableModel;

import java.util.HashMap;
import java.util.Map;

public class NamedCellsRowBuilder extends TableRowsBuilder<Map<String, CellModel>>
{
    private final TableModel tableModel;

    public NamedCellsRowBuilder(TableModel tableModel)
    {
        this.tableModel = tableModel;
    }

    @Override
    protected Map<String, CellModel> createRow(RowModel rowModel)
    {
        Map<String, CellModel> cells = new HashMap<>();
        for (CellModel cellModel : rowModel.getCells())
        {
            cells.put(cellModel.name, cellModel);
        }
        return cells;
    }

    @Override
    public TableModel getTableModel()
    {
        return tableModel;
    }
}
