package com.developmentontheedge.be5.server.services.document.rows;

import com.developmentontheedge.be5.server.model.table.CellModel;
import com.developmentontheedge.be5.server.model.table.RowModel;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class MoreRowsBuilder implements TableRowsBuilder<List<CellModel>>
{
    private final List<RowModel> rows;

    public MoreRowsBuilder(List<RowModel> rows)
    {
        this.rows = rows;
    }

    private CellModel createCell(CellModel cellModel)
    {
        return cellModel;
    }

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
    public List<List<CellModel>> build()
    {
        List<List<CellModel>> res = new ArrayList<>();
        for (RowModel rowModel : rows)
        {
            res.add(createRow(rowModel));
        }
        return res;
    }
}
