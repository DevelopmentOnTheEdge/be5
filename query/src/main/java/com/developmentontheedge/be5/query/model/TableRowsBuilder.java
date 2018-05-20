package com.developmentontheedge.be5.query.model;

import java.util.ArrayList;
import java.util.List;

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
