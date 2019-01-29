package com.developmentontheedge.be5.server.model.table;

import com.developmentontheedge.be5.query.model.CellModel;
import com.developmentontheedge.be5.query.model.RowModel;
import com.developmentontheedge.be5.query.model.TableModel;

import java.util.HashMap;
import java.util.Map;

public class NamedCellsRowBuilder extends TableRowsBuilder<Map<String, NamedCellModel>>
{
    private final TableModel tableModel;

    public NamedCellsRowBuilder(TableModel tableModel)
    {
        this.tableModel = tableModel;
    }

    @Override
    protected Map<String, NamedCellModel> createRow(RowModel rowModel)
    {
        Map<String, NamedCellModel> cells = new HashMap<>();
        for (CellModel cellModel : rowModel.getCells())
        {
            cells.put(cellModel.name, new NamedCellModel(
                    cellModel.name,
                    cellModel.title,
                    cellModel.content,
                    cellModel.options
            ));
        }
        return cells;
    }

    @Override
    public TableModel getTableModel()
    {
        return tableModel;
    }
}
