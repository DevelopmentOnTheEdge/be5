package com.developmentontheedge.be5.server.model.table;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;

public class NamedCellModel
{
    public final String name;
    public final String title;

    public final Object value;
    public final Map<String, Map<String, String>> options;

    public NamedCellModel(Object value)
    {
        this.name = null;
        this.title = null;
        this.value = value;
        this.options = new HashMap<>();
    }

    public NamedCellModel(String name, String title, Object value, Map<String, Map<String, String>> options)
    {
        this.name = name;
        this.title = title;
        this.value = value;
        this.options = options;
    }

    public Object getValue()
    {
        return value;
    }

    public Map<String, Map<String, String>> getOptions()
    {
        return options;
    }

    public NamedCellModel option(String type, String attribute, String value)
    {
        options.computeIfAbsent(type, k -> new HashMap<>());
        options.get(type).put(attribute, value);
        return this;
    }

    public NamedCellModel cssClasses(String value)
    {
        return option("css", "class", value);
    }

    public NamedCellModel grouping()
    {
        options.computeIfAbsent("grouping", k -> emptyMap());
        return this;
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
