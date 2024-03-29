package com.developmentontheedge.be5.modules.core.queries.system;

import com.developmentontheedge.be5.database.QRec;
import com.developmentontheedge.be5.server.queries.support.QueryExecutorSupport;

import java.util.Enumeration;
import java.util.List;
import java.util.Properties;


public class SystemPropertiesTable extends QueryExecutorSupport
{
    @Override
    public List<QRec> execute()
    {
        addColumns("name", "value");

        Properties p = System.getProperties();
        Enumeration keys = p.keys();
        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();

            addRow(cells(key, p.get(key).toString()));
        }
        return table();
    }

}
