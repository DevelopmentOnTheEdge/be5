package com.developmentontheedge.be5.server.model.table;

import com.developmentontheedge.be5.query.model.CellModel;
import com.developmentontheedge.be5.query.model.RowModel;
import com.developmentontheedge.be5.query.model.TableModel;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class MoreRowsBuilder extends TableRowsBuilder<List<CellModel>>
{
    private final TableModel tableModel;

    public MoreRowsBuilder(TableModel tableModel)
    {
        this.tableModel = tableModel;
    }

    private CellModel createCell(CellModel cellModel)
    {
        return cellModel;
    }

    @Override
    protected List<CellModel> createRow(RowModel rowModel)
    {
        List<CellModel> cells = new ArrayList<>();
        for (CellModel cellModel : rowModel.getCells())
        {
            cells.add(createCell(cellModel));
        }
        String id = rowModel.getId();
        return ImmutableList.<CellModel>builder()
                .add(new CellModel(id != null ? id : ""))
                .addAll(cells).build();
    }

    @Override
    public TableModel getTableModel()
    {
        return tableModel;
    }
}
