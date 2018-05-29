package com.developmentontheedge.be5.modules.system.queries;

import com.developmentontheedge.be5.queries.support.TableBuilderSupport;
import com.developmentontheedge.be5.query.model.TableModel;

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
