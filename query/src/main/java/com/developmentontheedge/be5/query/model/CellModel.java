package com.developmentontheedge.be5.query.model;

import javax.json.bind.annotation.JsonbTransient;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * TODO use {@link com.developmentontheedge.beans.DynamicProperty}
 */
@Deprecated
public class CellModel
{
    /**
     * A string or a list of strings.
     */
    @JsonbTransient
    public final String name;

    @JsonbTransient
    public final String title;

    public final Object content;
    public final Map<String, Map<String, String>> options;

    public CellModel(Object content)
    {
        this.name = null;
        this.title = null;
        this.content = content;
        this.options = new HashMap<>();
    }

    public CellModel(String name, String title, Object content, Map<String, Map<String, String>> options)
    {
        this.name = name;
        this.title = title;
        this.content = content;
        this.options = options;
    }

    public Object getContent()
    {
        return content;
    }

    public Map<String, Map<String, String>> getOptions()
    {
        return options;
    }

    public CellModel option(String type, String attribute, String value)
    {
        options.computeIfAbsent(type, k -> new HashMap<>());
        options.get(type).put(attribute, value);
        return this;
    }

    public CellModel cssClasses(String value)
    {
        return option("css", "class", value);
    }

    public CellModel grouping()
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
