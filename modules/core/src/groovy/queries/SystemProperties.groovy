import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.query.TableSupport


class SystemProperties extends TableSupport
{
    @Override
    TableModel getTable()
    {
        columns = columns("name", "value")

        Properties p = System.getProperties()
        Enumeration keys = p.keys()
        while (keys.hasMoreElements())
        {
            String key = (String)keys.nextElement()

            rows.add(row(cells(
                    key,
                    (String)p.get(key)
            )))
        }
        return table(columns, rows)
    }
}
