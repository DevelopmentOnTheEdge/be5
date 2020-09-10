package com.developmentontheedge.be5.server.model;

import java.util.List;
import java.util.Map;

public class RowsAsJsonPresentation
{
    private final List<Map<String, Object>> data;

    public RowsAsJsonPresentation(List<Map<String, Object>> data)
    {
        this.data = data;
    }

    public List<Map<String, Object>> getData()
    {
        return data;
    }

    @Override
    public String toString()
    {
        return "TablePresentation{" +
                ", rows=" + data +
                '}';
    }
}
