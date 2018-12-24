package com.developmentontheedge.be5.modules.core.queries.system;

import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.server.queries.support.QueryExecutorSupport;

import java.util.List;


public class SessionVariablesTable extends QueryExecutorSupport
{
    @Override
    public List<QRec> execute()
    {
        addColumns("Name", "Value");

        for (String name : session.getAttributeNames())
        {
            addRow(name, cells(name, session.get(name).toString()));
        }
        return table(true);
    }
}
