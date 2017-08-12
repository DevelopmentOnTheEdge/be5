import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.query.TableBuilderSupport


class DataSource extends TableBuilderSupport
{
    @Override
    TableModel getTable()
    {
        addColumns("name", "value")

        for (Map.Entry<String,String> entry : injector.getDatabaseService().getParameters())
        {
            addRow(cells(
                    entry.getKey(),
                    entry.getValue() != null ? entry.getValue() : ""
            ))
        }
        return table(columns, rows)
    }
}
