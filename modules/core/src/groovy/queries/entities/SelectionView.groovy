package entities

import com.developmentontheedge.be5.metadata.model.Entity
import com.developmentontheedge.be5.server.queries.support.DpsTableBuilderSupport
import com.developmentontheedge.beans.DynamicPropertySet

class SelectionView extends DpsTableBuilderSupport
{
    @Override
    List<DynamicPropertySet> getTableModel()
    {
        addColumns("Code", "Name")

        for (Entity entity : meta.getOrderedEntities(userInfo.getLanguage())) {
            addRow(cells(entity.getName(), entity.getName()))
        }

        return table()
    }
}
