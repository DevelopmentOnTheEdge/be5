package system

import com.developmentontheedge.be5.api.helpers.UserInfoHolder
import com.developmentontheedge.be5.util.ActionUtils
import com.developmentontheedge.be5.query.model.TableModel
import com.developmentontheedge.be5.query.model.CellModel
import com.developmentontheedge.be5.metadata.model.Query
import com.developmentontheedge.be5.queries.support.TableBuilderSupport


class Queries extends TableBuilderSupport
{
    @Override
    TableModel getTableModel()
    {
        addColumns("EntityName", "Name", "Type", "Roles", "Operations")

        def selectEntity = parameters.get("entity")

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
            Query query = meta.getQuery(entityName, queryName)
            List<CellModel> cells = new ArrayList<CellModel>()

            cells.add(new CellModel(entityName))
            cells.add(new CellModel(query.getName())
                    .add("link", "url", ActionUtils.toAction(query).arg))
            cells.add(new CellModel(query.getType()))
            cells.add(new CellModel(query.getRoles().getFinalRoles().toString()))
            cells.add(new CellModel(query.getOperationNames().getFinalValues().size().toString()))

            addRow(cells)
        }
    }
}
