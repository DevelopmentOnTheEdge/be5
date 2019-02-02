package com.developmentontheedge.be5.server.model.table;

import java.util.Map;

public class NamedCellModel
{
    public final String name;
    public final String title;

    public final Object value;
    public final Map<String, Map<String, String>> options;

    public NamedCellModel(String name, String title, Object value, Map<String, Map<String, String>> options)
    {
        this.name = name;
        this.title = title;
        this.value = value;
        this.options = options;
    }

    @Override
    public String toString()
    {
        return "CellModel{" +
                "value=" + value +
                ", options=" + options +
                '}';
    }
}
