package src.groovy.queries

import com.developmentontheedge.be5.server.queries.support.DpsTableBuilderSupport
import com.developmentontheedge.beans.DynamicPropertySet

class TestGroovyTable extends DpsTableBuilderSupport
{
    @Override
    List<DynamicPropertySet> getTableModel()
    {
        addColumns(userInfo.getUserName())

        addRow(cells(userInfo.getCurrentRoles().toString()))

        return table()
    }
}
