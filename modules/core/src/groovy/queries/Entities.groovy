import com.developmentontheedge.be5.api.helpers.UserInfoHolder
import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.components.FrontendConstants
import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.components.impl.model.TableModel.CellModel
import com.developmentontheedge.be5.metadata.DatabaseConstants
import com.developmentontheedge.be5.metadata.model.Entity
import com.developmentontheedge.be5.query.TableSupport
import com.developmentontheedge.be5.util.HashUrl

class Entities extends TableSupport
{
    @Override
    TableModel get()
    {
        columns = getColumns("Name","Type",
                "Columns",
                "Queries", "Operations")
        Meta meta = injector.getMeta()

        def entities = meta.getOrderedEntities(UserInfoHolder.language)
        for (Entity entity : entities) {
            List<CellModel> cells = new ArrayList<CellModel>()

            Map<String, Map<String,String>> options = new HashMap<>()
            if(entity.getQueries().get(DatabaseConstants.ALL_RECORDS_VIEW) != null){
                options.put("link", Collections.singletonMap("url",
                        new HashUrl(FrontendConstants.TABLE_ACTION,
                                entity.getName(),
                                DatabaseConstants.ALL_RECORDS_VIEW
                        ).toString()
                ))
            }

            cells.add(new CellModel(entity.getName(), options ))

            cells.add(new CellModel(entity.getTypeString()))
            cells.add(new CellModel(meta.getColumns(entity).size()))
            cells.add(new CellModel(meta.getQueryNames(entity).size(),
                    Collections.singletonMap("link", Collections.singletonMap("url",
                            new HashUrl(FrontendConstants.TABLE_ACTION,
                                "_system_",    "Queries"
                            ).named("entity", entity.getName()).toString()))))
            cells.add(new CellModel(meta.getOperationNames(entity).size()))

            rows.add(getRow(cells))
        }

        return getTable(columns, rows)
    }
}
