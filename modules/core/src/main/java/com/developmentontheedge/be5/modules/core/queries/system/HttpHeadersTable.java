package com.developmentontheedge.be5.modules.core.queries.system;

import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.server.queries.support.QueryExecutorSupport;

import java.util.Enumeration;
import java.util.List;


public class HttpHeadersTable extends QueryExecutorSupport
{
    @Override
    @SuppressWarnings("unchecked")
    public List<QRec> execute()
    {
        addColumns("name", "value");

        Enumeration<String> keys = request.getRawRequest().getHeaderNames();
        if (keys != null)
        {
            while (keys.hasMoreElements())
            {
                String key = keys.nextElement();
                addRow(cells(key, request.getRawRequest().getHeader(key)));
            }
        }
        return table();
    }
}
