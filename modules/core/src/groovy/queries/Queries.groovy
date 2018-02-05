import com.developmentontheedge.be5.api.helpers.UserInfoHolder
import com.developmentontheedge.be5.components.impl.model.ActionHelper
import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.components.impl.model.TableModel.CellModel
import com.developmentontheedge.be5.metadata.model.Query
import com.developmentontheedge.be5.query.TableBuilderSupport


class Queries extends TableBuilderSupport
{
    @Override
    TableModel getTableModel()
    {
        addColumns("EntityName","Name","Type", "Roles", "Operations")

        def selectEntity = parametersMap.get("entity")

        if(selectEntity)
        {
            addQueries(selectEntity)
        }
        else
        {
            meta.getOrderedEntities(UserInfoHolder.getLanguage()).forEach(
                    {e -> addQueries(e.getName())}
            )
        }

        return table(columns, rows)
    }

    void addQueries(String entityName)
    {
        def queries = meta.getQueryNames(meta.getEntity(entityName))
        for (String queryName: queries)
        {
            Query query = meta.getQueryIgnoringRoles(entityName, queryName)
            List<CellModel> cells = new ArrayList<CellModel>()

            cells.add(new CellModel(entityName))
            cells.add(new CellModel(query.getName())
                    .add("link", "url", ActionHelper.toAction(query).arg))
            cells.add(new CellModel(query.getType()))
            cells.add(new CellModel(query.getRoles().getFinalRoles().toString()))
            cells.add(new CellModel(query.getOperationNames().getFinalValues().size().toString()))

            addRow(cells)
        }
    }
}
