import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.query.TableSupport


class SessionVariables extends TableSupport
{
    @Override
    TableModel get()
    {
        def session = req.getRawSession()
        columns = getColumns("name", "value")

        Enumeration it = session.getAttributeNames();
        while (it.hasMoreElements())
        {
            String name = (String)it.nextElement()

            rows.add(getRow(getCells(
                    name,
                    session.getAttribute(name).toString()
            )))
        }

        return getTable(columns, rows)
    }
}
