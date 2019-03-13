package com.developmentontheedge.be5.query.support;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;

public class Cell
{
    public final Object content;
    public final Map<String, Map<String, String>> options;

    public Cell(Object content)
    {
        this.content = content;
        this.options = new HashMap<>();
    }

    public Object getContent()
    {
        return content;
    }

    public Map<String, Map<String, String>> getOptions()
    {
        return options;
    }

    public Cell option(String type, String attribute, String value)
    {
        options.computeIfAbsent(type, k -> new HashMap<>());
        options.get(type).put(attribute, value);
        return this;
    }

    public Cell cssClasses(String value)
    {
        return option("css", "class", value);
    }

    public Cell link(String url)
    {
        return option("link", "url", url);
    }

    public Cell grouping()
    {
        options.computeIfAbsent("grouping", k -> emptyMap());
        return this;
    }

    @Override
    public String toString()
    {
        return "CellModel{" +
                "content=" + content +
                ", options=" + options +
                '}';
    }
}
