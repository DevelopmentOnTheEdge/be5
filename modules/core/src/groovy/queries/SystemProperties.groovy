import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.query.TableSupport


class SystemProperties extends TableSupport
{
    @Override
    TableModel get()
    {
        columns = getColumns("name", "value")

        Properties p = System.getProperties()
        Enumeration keys = p.keys()
        while (keys.hasMoreElements())
        {
            String key = (String)keys.nextElement()

            rows.add(getRow(getCells(
                    key,
                    (String)p.get(key)
            )))
        }
        return getTable(columns, rows)
    }
}
