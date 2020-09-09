package com.developmentontheedge.be5.server.model;

import java.util.List;
import java.util.Map;

public class RowsAsJsonPresentation
{
    private final List<Map<String, Object>> rows;

    public RowsAsJsonPresentation(List<Map<String, Object>> rows)
    {
        this.rows = rows;
    }

    public List<Map<String, Object>> getRows()
    {
        return rows;
    }

    @Override
    public String toString()
    {
        return "TablePresentation{" +
                ", rows=" + rows +
                '}';
    }
}
