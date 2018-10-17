package com.developmentontheedge.be5.query.model;

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

    public ColumnModel(String name, String title)
    {
        this(name, title, null);
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
