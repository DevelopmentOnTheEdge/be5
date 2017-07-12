package com.developmentontheedge.be5.components.impl;

import java.util.List;

public class MoreRows {
    
    private final int draw;
    private final int recordsTotal;
    private final int recordsFiltered;
    private final List<List<Object>> data; // rows
    
    public MoreRows(int draw, int recordsTotal, int recordsFiltered, List<List<Object>> data)
    {
        this.draw = draw;
        this.recordsTotal = recordsTotal;
        this.recordsFiltered = recordsFiltered;
        this.data = data;
    }

    public int getDraw()
    {
        return draw;
    }

    public int getRecordsTotal()
    {
        return recordsTotal;
    }

    public int getRecordsFiltered()
    {
        return recordsFiltered;
    }

    public List<List<Object>> getData()
    {
        return data;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MoreRows moreRows = (MoreRows) o;

        if (draw != moreRows.draw) return false;
        if (recordsTotal != moreRows.recordsTotal) return false;
        if (recordsFiltered != moreRows.recordsFiltered) return false;
        return data != null ? data.equals(moreRows.data) : moreRows.data == null;
    }

    @Override
    public int hashCode()
    {
        int result = draw;
        result = 31 * result + recordsTotal;
        result = 31 * result + recordsFiltered;
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }
}