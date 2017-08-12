import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.query.TableSupport


class DataSource extends TableSupport
{
    @Override
    TableModel getTable()
    {
        columns = columns("name", "value")

        for (Map.Entry<String,String> entry : injector.getDatabaseService().getParameters())
        {
            rows.add(row(cells(
                    entry.getKey(),
                    entry.getValue() != null ? entry.getValue() : ""
            )))
        }
        return table(columns, rows)
    }
}
