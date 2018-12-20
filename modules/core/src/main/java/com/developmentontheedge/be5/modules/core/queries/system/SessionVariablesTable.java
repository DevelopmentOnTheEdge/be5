package com.developmentontheedge.be5.modules.core.queries.system;

import com.developmentontheedge.be5.server.queries.support.QueryExecutorSupport;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.List;


public class SessionVariablesTable extends QueryExecutorSupport
{
    @Override
    public List<DynamicPropertySet> execute()
    {
        addColumns("Name", "Value");

        for (String name : session.getAttributeNames())
        {
            addRow(name, cells(name, session.get(name).toString()));
        }
        return table(true);
    }
}
