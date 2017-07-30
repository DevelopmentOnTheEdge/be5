import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.query.TableSupport

class Pool extends TableSupport
{
    @Override
    TableModel get()
    {
        def statistics = injector.getDatabaseService().getConnectionsStatistics()

        columns = getColumns("statistics")
        rows.add(getRow(getCells(statistics)))

        return getTable(columns, rows)
    }
}
