package roles

import com.developmentontheedge.be5.server.queries.support.DpsTableBuilderSupport
import com.developmentontheedge.beans.DynamicPropertySet

class SelectionView extends DpsTableBuilderSupport
{
    @Override
    List<DynamicPropertySet> getTableModel()
    {
        addColumns("Code", "Name")

        for (def role : meta.getProjectRoles()) {
            addRow(cells(role, role))
        }

        return table()
    }
}
