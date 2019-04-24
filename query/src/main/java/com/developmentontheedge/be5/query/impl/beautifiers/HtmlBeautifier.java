package com.developmentontheedge.be5.query.impl.beautifiers;

import com.developmentontheedge.be5.query.model.beans.QRec;
import one.util.streamex.StreamEx;

import java.util.List;

public abstract class HtmlBeautifier implements SubQueryBeautifier
{
    @Override
    public String print(List<QRec> lists)
    {
        return StreamEx.of(lists)
                .map(this::toRow)
                .joining(getRowDelimiter());
    }

    private String toRow(QRec qRec)
    {
        return StreamEx.of(qRec.spliterator())
                .map(entry -> entry.getValue().toString())
                .joining(getColumnDelimiter());
    }

    public abstract String getRowDelimiter();

    public abstract String getColumnDelimiter();
}
