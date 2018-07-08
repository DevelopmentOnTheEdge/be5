package src.groovy.queries

import com.developmentontheedge.be5.query.model.TableModel
import com.developmentontheedge.be5.server.queries.support.TableBuilderSupport


class TestGroovyTable extends TableBuilderSupport {
    @Override
    TableModel getTableModel() {
        addColumns(userInfo.getUserName())

        addRow(cells(userInfo.getCurrentRoles().toString()))

        return table(columns, rows)
    }
}
