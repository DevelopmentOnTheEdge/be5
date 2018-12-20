package src.groovy.queries

import com.developmentontheedge.be5.server.queries.support.QueryExecutorSupport
import com.developmentontheedge.beans.DynamicPropertySet

class TestGroovyTable extends QueryExecutorSupport
{
    @Override
    List<DynamicPropertySet> execute()
    {
        addColumns(userInfo.getUserName())

        addRow(cells(userInfo.getCurrentRoles().toString()))

        return table()
    }
}
