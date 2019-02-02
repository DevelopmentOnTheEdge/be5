package com.developmentontheedge.be5.server.model.table;

public class ColumnModel
{
    private final String title;
    private final String name;
    private final String quick;

    public ColumnModel(String name, String title, String quick)
    {
        this.title = title;
        this.name = name;
        this.quick = quick;
    }

    public String getName()
    {
        return name;
    }

    public String getTitle()
    {
        return title;
    }

    public String getQuick()
    {
        return quick;
    }
}
