package com.developmentontheedge.be5.query.impl.beautifiers;

import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Map;

public abstract class HtmlBeautifier implements SubQueryBeautifier
{
    @Override
    public String print(List<Map<String, Object>> lists)
    {
        return StreamEx.of(lists)
                .map(values -> StreamEx.of(values.entrySet())
                        .map(entry -> entry.getValue().toString())
                        .joining(getColumnDelimiter()))
                .joining(getRowDelimiter());
    }

    public abstract String getRowDelimiter();

    public abstract String getColumnDelimiter();
}
