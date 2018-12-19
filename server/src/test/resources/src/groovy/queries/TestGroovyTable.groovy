package src.groovy.queries

import com.developmentontheedge.be5.query.model.TableModel
import com.developmentontheedge.be5.server.queries.support.DpsTableBuilderSupport

class TestGroovyTable extends DpsTableBuilderSupport
{
    @Override
    TableModel getTableModel()
    {
        addColumns(userInfo.getUserName())

        addRow(cells(userInfo.getCurrentRoles().toString()))

        return table()
    }
}
