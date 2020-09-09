package com.developmentontheedge.be5.server.services.document.rows;

import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.beans.DynamicProperty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RowsAsJsonBuilder implements TableRowsBuilder<Map<String, Object>>
{
    private final List<QRec> rows;

    public RowsAsJsonBuilder(List<QRec> rows)
    {
        this.rows = rows;
    }

    @Override
    public List<Map<String, Object>> build()
    {
        List<Map<String, Object>> res = new ArrayList<>();
        for (QRec qRec : rows)
        {
            Map<String, Object> row = new LinkedHashMap<>();
            for (DynamicProperty dp : qRec)
            {
                row.put(dp.getName(), dp.getValue());
            }
            res.add(row);
        }
        return res;
    }
}
