package com.developmentontheedge.be5.modules.core.queries.system;

import com.developmentontheedge.be5.server.queries.support.DpsTableBuilderSupport;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.Enumeration;
import java.util.List;


public class HttpHeadersTable extends DpsTableBuilderSupport
{
    @Override
    @SuppressWarnings("unchecked")
    public List<DynamicPropertySet> getTableModel()
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
