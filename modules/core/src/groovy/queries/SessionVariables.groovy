import com.developmentontheedge.be5.query.impl.model.TableModel
import com.developmentontheedge.be5.query.TableBuilderSupport


class SessionVariables extends TableBuilderSupport
{
    @Override
    TableModel getTableModel()
    {
        addColumns("Name", "Value")

        for(String name : session.getAttributeNames())
        {
            addRow(name, cells( name, session.get(name).toString() ))
        }

        return table(columns, rows, true)
    }
}
