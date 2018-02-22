package src.groovy.queries

import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.query.TableBuilderSupport

class TestGroovyTable extends TableBuilderSupport
{
    @Override
    TableModel getTableModel()
    {
        addColumns("name", "value")

        addRow(cells("a1", "b1"))
        addRow(cells("a2", "b2"))

        return table(columns, rows)
    }
}