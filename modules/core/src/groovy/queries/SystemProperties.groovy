import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.query.TableBuilderSupport


class SystemProperties extends TableBuilderSupport
{
    @Override
    TableModel getTable()
    {
        addColumns("name", "value")

        Properties p = System.getProperties()
        Enumeration keys = p.keys()
        while (keys.hasMoreElements())
        {
            String key = (String)keys.nextElement()

            addRow(cells( key, p.get(key).toString() ))
        }
        return table(columns, rows)
    }
}
