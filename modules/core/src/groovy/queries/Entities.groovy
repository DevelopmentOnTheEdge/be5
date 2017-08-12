import com.developmentontheedge.be5.api.helpers.UserInfoHolder
import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.components.FrontendConstants
import com.developmentontheedge.be5.components.impl.model.ActionHelper
import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.components.impl.model.TableModel.CellModel
import com.developmentontheedge.be5.metadata.DatabaseConstants
import com.developmentontheedge.be5.metadata.model.Entity
import com.developmentontheedge.be5.query.TableBuilderSupport
import com.developmentontheedge.be5.util.HashUrl

class Entities extends TableBuilderSupport
{
    @Override
    TableModel getTable()
    {
        addColumns("Name","Type", "Columns", "Queries", "Operations")
        Meta meta = injector.getMeta()

        def entities = meta.getOrderedEntities(UserInfoHolder.language)
        for (Entity entity : entities) {
            List<CellModel> cells = new ArrayList<CellModel>()

            def name = new CellModel(entity.getName())
            def allRecords = entity.getQueries().get(DatabaseConstants.ALL_RECORDS_VIEW)
            if(allRecords != null)name.add("link", "url", ActionHelper.toAction(allRecords).arg)

            cells.add(name)
            cells.add(new CellModel(entity.getTypeString()))
            cells.add(new CellModel(meta.getColumns(entity).size()))

            cells.add(new CellModel(meta.getQueryNames(entity).size()).add("link", "url",
                        new HashUrl(FrontendConstants.TABLE_ACTION, "_system_", "Queries")
                        .named("entity", entity.getName()).toString()))
            cells.add(new CellModel(meta.getOperationNames(entity).size()))

            addRow(cells)
        }

        return table(columns, rows)
    }
}
