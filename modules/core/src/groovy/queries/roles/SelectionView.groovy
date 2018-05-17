package roles

import com.developmentontheedge.be5.queries.support.TableBuilderSupport
import com.developmentontheedge.be5.query.impl.TableModel


class SelectionView extends TableBuilderSupport
{
    @Override
    TableModel getTableModel()
    {
        addColumns("Code","Name")

        for(def role : meta.getProjectRoles())
        {
            addRow(cells( role, role ))
        }

        return table(columns, rows)
    }
}
