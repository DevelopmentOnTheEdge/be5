package com.developmentontheedge.be5.server.model.table;

import java.util.HashMap;
import java.util.Map;

public class RawCellModel
{
    public final String name;
    public final String title;
    public final Object content;
    public final Map<String, Map<String, String>> options;
    public final boolean hidden;

    public RawCellModel(String name, String title, Object content, Map<String, Map<String, String>> options, boolean hidden)
    {
        this.name = name;
        this.title = title;
        this.content = content;
        this.options = options;
        this.hidden = hidden;
    }

    public RawCellModel(Object content)
    {
        this.name = "";
        this.title = "";
        this.content = content;
        this.options = new HashMap<>();
        this.hidden = false;
    }

}
