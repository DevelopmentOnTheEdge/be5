package roles

import com.developmentontheedge.be5.server.queries.support.QueryBuilderSupport
import com.developmentontheedge.beans.DynamicPropertySet

class SelectionView extends QueryBuilderSupport
{
    @Override
    List<DynamicPropertySet> execute()
    {
        addColumns("Code", "Name")

        for (def role : meta.getProjectRoles()) {
            addRow(cells(role, role))
        }

        return table()
    }
}
