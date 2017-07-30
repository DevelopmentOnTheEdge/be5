import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.query.TableSupport


class DataSource extends TableSupport
{
    @Override
    TableModel get()
    {
        columns = getColumns("name", "value")

        for (Map.Entry<String,String> entry : injector.getDatabaseService().getParameters())
        {
            rows.add(getRow(getCells(
                    entry.getKey(),
                    entry.getValue() != null ? entry.getValue() : ""
            )))
        }
        return getTable(columns, rows)
    }
}
