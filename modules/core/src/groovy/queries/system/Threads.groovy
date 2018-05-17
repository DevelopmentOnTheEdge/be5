package system

import com.developmentontheedge.be5.query.impl.TableModel
import com.developmentontheedge.be5.queries.support.TableBuilderSupport


class Threads extends TableBuilderSupport
{
    @Override
    TableModel getTableModel()
    {
        addColumns("name", "groupName",  "state", "alive", "priority", "threadGroup", "id")
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet()

        for (Thread thread : threadSet)
        {
            addRow(cells(
                    thread.name.toString(),
                    thread.threadGroup.getName(),
                    thread.state.toString(),
                    thread.alive.toString(),
                    thread.priority.toString(),
                    thread.threadGroup.toString(),
                    thread.id.toString(),
            ))
        }

        return table(columns, rows)
    }
}
