package com.developmentontheedge.be5.server.model;

import com.developmentontheedge.be5.server.model.table.CellModel;

import java.util.List;

public class MoreRowsPresentation
{
    private final long recordsTotal;
    private final long recordsFiltered;
    private final List<List<CellModel>> data;

    public MoreRowsPresentation(long recordsTotal, long recordsFiltered, List<List<CellModel>> data)
    {
        this.recordsTotal = recordsTotal;
        this.recordsFiltered = recordsFiltered;
        this.data = data;
    }

    public long getRecordsTotal()
    {
        return recordsTotal;
    }

    public long getRecordsFiltered()
    {
        return recordsFiltered;
    }

    public List<List<CellModel>> getData()
    {
        return data;
    }
}
