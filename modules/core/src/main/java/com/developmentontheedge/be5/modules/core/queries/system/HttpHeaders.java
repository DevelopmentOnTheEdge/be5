package com.developmentontheedge.be5.modules.core.queries.system;

import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.server.queries.support.TableBuilderSupport;

import java.util.Enumeration;


public class HttpHeaders extends TableBuilderSupport
{
    @Override
    public TableModel getTableModel()
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

        return table(columns, rows);
    }

}