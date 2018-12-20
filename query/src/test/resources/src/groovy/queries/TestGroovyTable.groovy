package src.groovy.queries

import com.developmentontheedge.be5.query.support.BaseDpsTableBuilderSupport
import com.developmentontheedge.beans.DynamicPropertySet

class TestGroovyTable extends BaseDpsTableBuilderSupport
{
    @Override
    List<DynamicPropertySet> getTableModel()
    {
        addColumns("name", "value")

        addRow(1, cells("a1", "b1"))
        addRow(2, cells("a2", "b2"))

        return table()
    }
}
