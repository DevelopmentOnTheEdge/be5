import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.query.TableBuilderSupport


class SessionVariables extends TableBuilderSupport
{
    @Override
    TableModel getTable()
    {
        addColumns("name", "value")

        def session = req.getRawSession()
        Enumeration it = session.getAttributeNames();
        while (it.hasMoreElements())
        {
            String name = (String)it.nextElement()

            addRow(cells(
                    name,
                    session.getAttribute(name).toString()
            ))
        }

        return table(columns, rows)
    }
}
