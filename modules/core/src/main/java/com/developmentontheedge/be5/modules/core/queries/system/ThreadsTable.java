package com.developmentontheedge.be5.modules.core.queries.system;

import com.developmentontheedge.be5.server.queries.support.DpsTableBuilderSupport;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.List;


public class ThreadsTable extends DpsTableBuilderSupport
{
    @Override
    public List<DynamicPropertySet> getTableModel()
    {
        addColumns("name", "groupName", "state", "alive", "priority", "threadGroup", "id");

        for (Thread thread : Thread.getAllStackTraces().keySet())
        {
            addRow(cells(
                    thread.getName(),
                    thread.getThreadGroup().getName(),
                    thread.getState().toString(),
                    thread.isAlive(),
                    thread.getPriority(),
                    thread.getThreadGroup().toString(),
                    thread.getId()
            ));
        }
        return table();
    }

}
