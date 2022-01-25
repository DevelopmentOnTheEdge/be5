package com.developmentontheedge.be5.server.services.document.rows;

import com.developmentontheedge.be5.database.QRec;
import com.developmentontheedge.be5.database.util.DynamicPropertyMeta;
import com.developmentontheedge.be5.server.model.table.NamedCellModel;
import com.developmentontheedge.beans.DynamicProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NamedCellsRowBuilder implements TableRowsBuilder<Map<String, NamedCellModel>>
{
    private final List<QRec> rows;

    public NamedCellsRowBuilder(List<QRec> rows)
    {
        this.rows = rows;
    }

    protected Map<String, NamedCellModel> createRow(QRec row)
    {
        Map<String, NamedCellModel> cells = new HashMap<>();
        for (DynamicProperty cell : row)
        {
            cells.put(cell.getName(), new NamedCellModel(
                    cell.getName(),
                    cell.getDisplayName(),
                    cell.getValue(),
                    DynamicPropertyMeta.get(cell)
            ));
        }
        return cells;
    }

    @Override
    public List<Map<String, NamedCellModel>> build()
    {
        List<Map<String, NamedCellModel>> res = new ArrayList<>();
        for (QRec qRec : rows)
        {
            res.add(createRow(qRec));
        }
        return res;
    }
}
