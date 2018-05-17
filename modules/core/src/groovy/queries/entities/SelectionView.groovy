package entities

import com.developmentontheedge.be5.metadata.model.Entity
import com.developmentontheedge.be5.queries.support.TableBuilderSupport
import com.developmentontheedge.be5.query.impl.TableModel


class SelectionView extends TableBuilderSupport
{
    @Override
    TableModel getTableModel()
    {
        addColumns("Code","Name")

        for (Entity entity : meta.getOrderedEntities(userInfo.getLanguage()))
        {
            addRow(cells( entity.getName(), entity.getName() ))
        }

        return table(columns, rows)
    }
}
