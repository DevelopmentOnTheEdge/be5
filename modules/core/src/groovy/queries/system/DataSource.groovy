package system

import com.developmentontheedge.be5.query.impl.TableModel
import com.developmentontheedge.be5.queries.support.TableBuilderSupport


class DataSource extends TableBuilderSupport
{
    @Override
    TableModel getTableModel()
    {
        addColumns("name", "value")

        for (Map.Entry<String,String> entry : databaseService.getParameters())
        {
            addRow(cells(
                    entry.getKey(),
                    entry.getValue() != null ? entry.getValue() : ""
            ))
        }
        return table(columns, rows)
    }
}
