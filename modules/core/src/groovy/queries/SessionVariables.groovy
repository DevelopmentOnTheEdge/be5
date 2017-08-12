import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.query.TableSupport


class SessionVariables extends TableSupport
{
    @Override
    TableModel getTable()
    {
        def session = req.getRawSession()
        columns = columns("name", "value")

        Enumeration it = session.getAttributeNames();
        while (it.hasMoreElements())
        {
            String name = (String)it.nextElement()

            rows.add(row(cells(
                    name,
                    session.getAttribute(name).toString()
            )))
        }

        return table(columns, rows)
    }
}
