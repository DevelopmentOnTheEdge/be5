package com.developmentontheedge.be5.components.impl;

import java.util.ArrayList;
import java.util.List;

import com.developmentontheedge.be5.components.impl.model.TableModel;
import com.developmentontheedge.be5.components.impl.model.TableModel.CellModel;
import com.developmentontheedge.be5.components.impl.model.TableModel.RowModel;

public abstract class TableRowsBuilder<RowT, CellT> {
    
    public TableRowsBuilder() {
    }
    
    public List<RowT> build(TableModel tableModel) {
        List<RowT> rows = new ArrayList<>();
        
        for (RowModel rowModel : tableModel.getRows())
        {
            List<CellT> cells = new ArrayList<>();
            for (CellModel cellModel : rowModel.getCells())
            {
                cells.add(createCell(cellModel));
            }
            rows.add(createRow(rowModel, cells));
        }
        
        return rows;
    }
    
    protected abstract CellT createCell(CellModel cellModel);
    protected abstract RowT createRow(RowModel rowModel, List<CellT> cells);
    
}
