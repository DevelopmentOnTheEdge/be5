import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.query.TableBuilderSupport


class SessionVariables extends TableBuilderSupport
{
    @Override
    TableModel getTable()
    {
        addColumns("Name", "Value")

        def session = req.getRawSession()
        Enumeration it = session.getAttributeNames();
        while (it.hasMoreElements())
        {
            String name = (String)it.nextElement()
            addRow(name, cells( name, session.getAttribute(name).toString() ))
        }

        return table(columns, rows, true)
    }
}
