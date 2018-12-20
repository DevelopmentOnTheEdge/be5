package com.developmentontheedge.be5.modules.core.queries.system;

import com.developmentontheedge.be5.server.queries.support.QueryExecutorSupport;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.Enumeration;
import java.util.List;


public class HttpHeadersTable extends QueryExecutorSupport
{
    @Override
    @SuppressWarnings("unchecked")
    public List<DynamicPropertySet> execute()
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
