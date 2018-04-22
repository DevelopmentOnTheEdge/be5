package com.developmentontheedge.be5.query.model;

import java.util.List;

import com.developmentontheedge.be5.query.impl.TableModel.CellModel;
import com.developmentontheedge.be5.query.impl.TableModel.RowModel;

public class InitialRowsBuilder extends TableRowsBuilder<InitialRow, Object>
{

    private final boolean selectable;

    public InitialRowsBuilder(boolean selectable)
    {
        this.selectable = selectable;
    }

    @Override
    protected Object createCell(CellModel cellModel)
    {
        return cellModel;
    }

    @Override
    protected InitialRow createRow(RowModel rowModel, List<Object> cells)
    {
        return new InitialRow(selectable ? rowModel.getId() : null, cells);
    }

}