package com.developmentontheedge.be5.modules.core.queries.system;

import com.developmentontheedge.be5.database.QRec;
import com.developmentontheedge.be5.server.queries.support.QueryExecutorSupport;

import java.util.List;


public class ThreadsTable extends QueryExecutorSupport
{
    @Override
    public List<QRec> execute()
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
