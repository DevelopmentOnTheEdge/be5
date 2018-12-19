package src.groovy.queries

import com.developmentontheedge.be5.query.model.TableModel
import com.developmentontheedge.be5.query.support.BaseDpsTableBuilderSupport

class TestGroovyTable extends BaseDpsTableBuilderSupport
{
    @Override
    TableModel getTableModel()
    {
        addColumns("name", "value")

        addRow(cells("a1", "b1"))
        addRow(cells("a2", "b2"))

        return table()
    }
}
