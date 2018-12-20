package entities

import com.developmentontheedge.be5.metadata.model.Entity
import com.developmentontheedge.be5.server.queries.support.QueryBuilderSupport
import com.developmentontheedge.beans.DynamicPropertySet

class SelectionView extends QueryBuilderSupport
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
