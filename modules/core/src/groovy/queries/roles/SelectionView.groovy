package roles

import com.developmentontheedge.be5.query.model.TableModel
import com.developmentontheedge.be5.server.queries.support.TableBuilderSupport

class SelectionView extends TableBuilderSupport
{
    @Override
    TableModel getTableModel()
    {
        addColumns("Code", "Name")

        for (def role : meta.getProjectRoles()) {
            addRow(cells(role, role))
        }

        return table(columns, rows)
    }
}
