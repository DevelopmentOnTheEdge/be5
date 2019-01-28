package com.developmentontheedge.be5.server.model.table;

import com.developmentontheedge.be5.query.model.RowModel;
import com.developmentontheedge.be5.query.model.TableModel;

import java.util.ArrayList;
import java.util.List;

public abstract class TableRowsBuilder<RowT>
{
    public TableRowsBuilder()
    {
    }

    public List<RowT> build()
    {
        List<RowT> rows = new ArrayList<>();
        for (RowModel rowModel : getTableModel().getRows())
        {
            rows.add(createRow(rowModel));
        }
        return rows;
    }

    protected abstract RowT createRow(RowModel rowModel);

    public abstract TableModel getTableModel();
}
