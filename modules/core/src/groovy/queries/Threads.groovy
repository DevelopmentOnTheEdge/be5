import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.query.TableSupport


class Threads extends TableSupport
{
    @Override
    TableModel getTable()
    {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet()
        columns = columns("name", "groupName",  "state", "alive", "priority", "threadGroup","id")


        for (Thread thread : threadSet)
        {
            rows.add(row(cells(
                    thread.name.toString(),
                    thread.threadGroup.getName(),
                    thread.state.toString(),
                    thread.alive.toString(),
                    thread.priority.toString(),
                    thread.threadGroup.toString(),
                    thread.id.toString(),
            )))
            //thread.stackTrace
        }

        return table(columns, rows)
    }
}
