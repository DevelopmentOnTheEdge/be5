package roles

import com.developmentontheedge.be5.server.queries.support.TableBuilderSupport
import com.developmentontheedge.be5.query.model.TableModel


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
