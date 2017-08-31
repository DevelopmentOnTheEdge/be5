import com.developmentontheedge.be5.components.impl.model.ActionHelper
import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.components.impl.model.TableModel.CellModel
import com.developmentontheedge.be5.metadata.model.Query
import com.developmentontheedge.be5.query.TableBuilderSupport

class Queries extends TableBuilderSupport
{
    @Override
    TableModel getTable()
    {
        addColumns("Name","Type", "Roles", "Operations")

        def queries = meta.getQueryNames(meta.getEntity(parametersMap.get("entity")))

        for (String queryName: queries) {
            Query query = meta.getQueryIgnoringRoles(parametersMap.get("entity"), queryName)
            List<CellModel> cells = new ArrayList<CellModel>()

            cells.add(new CellModel(query.getName())
                    .add("link", "url", ActionHelper.toAction(query).arg))
            cells.add(new CellModel(query.getType()))
            cells.add(new CellModel(query.getRoles().getFinalRoles().toString()))
            cells.add(new CellModel(query.getOperationNames().getFinalValues().size().toString()))

            addRow(cells)
        }

        return table(columns, rows)
    }
}
