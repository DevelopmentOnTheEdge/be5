package com.developmentontheedge.be5.modules.system.queries;

import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.server.queries.support.TableBuilderSupport;

import java.util.Set;


public class Threads extends TableBuilderSupport
{
    @Override
    public TableModel getTableModel()
    {
        addColumns("name", "groupName", "state", "alive", "priority", "threadGroup", "id");
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();

        for (Thread thread : threadSet)
        {
            addRow(cells(
                    thread.getName(),
                    thread.getThreadGroup().getName(),
                    thread.getState().toString(),
                    thread.isAlive(),
                    thread.getPriority(),
                    thread.getThreadGroup().toString(),
                    thread.getId()));
        }

        return table(columns, rows);
    }

}
