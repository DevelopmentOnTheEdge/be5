package com.developmentontheedge.be5.modules.system.queries;

import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.server.queries.support.TableBuilderSupport;


public class SessionVariables extends TableBuilderSupport
{
    @Override
    public TableModel getTableModel()
    {
        addColumns("Name", "Value");

        for (String name : session.getAttributeNames())
        {
            addRow(name, cells(name, session.get(name).toString()));
        }


        return table(columns, rows, true);
    }

}
