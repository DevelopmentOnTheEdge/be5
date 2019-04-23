package com.developmentontheedge.be5.query.impl.beautifiers;

public class HtmlGlueBeautifier extends HtmlBeautifier
{
    protected String rowDelimiter = "<br/>";
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
