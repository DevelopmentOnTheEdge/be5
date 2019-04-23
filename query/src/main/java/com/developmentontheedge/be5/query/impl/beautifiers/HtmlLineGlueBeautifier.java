package com.developmentontheedge.be5.query.impl.beautifiers;

public class HtmlLineGlueBeautifier extends HtmlBeautifier
{
    protected String rowDelimiter = ", ";
    protected String columnDelimiter = ", ";

    public String getRowDelimiter()
    {
        return rowDelimiter;
    }

    public String getColumnDelimiter()
    {
        return columnDelimiter;
    }
}
