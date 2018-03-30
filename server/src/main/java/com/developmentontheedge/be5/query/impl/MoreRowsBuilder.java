package com.developmentontheedge.be5.query.impl;

import java.util.List;

import com.developmentontheedge.be5.query.impl.model.TableModel.CellModel;
import com.developmentontheedge.be5.query.impl.model.TableModel.RowModel;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class MoreRowsBuilder extends TableRowsBuilder<List<Object>, Object> {
    
    private final boolean selectable;

    public MoreRowsBuilder(boolean selectable)
    {
        this.selectable = selectable;
    }

    @Override
    protected Object createCell(CellModel cellModel)
    {
        return cellModel;
    }

    @Override
    protected List<Object> createRow(RowModel rowModel, List<Object> cells)
    {
        Builder<Object> builder = ImmutableList.builder();

        if (selectable)
        {
            builder = builder.add(rowModel.getId());
        }

        return builder.addAll(cells).build();
    }

}
