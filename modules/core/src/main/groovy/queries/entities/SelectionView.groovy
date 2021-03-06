package queries.entities

import com.developmentontheedge.be5.metadata.model.Entity
import com.developmentontheedge.be5.server.queries.support.QueryExecutorSupport
import com.developmentontheedge.beans.DynamicPropertySet

class SelectionView extends QueryExecutorSupport
{
    @Override
    List<DynamicPropertySet> execute()
    {
        addColumns("Code", "Name")

        for (Entity entity : meta.getOrderedEntities(userInfo.getLanguage())) {
            addRow(cells(entity.getName(), entity.getName()))
        }

        return table()
    }
}
