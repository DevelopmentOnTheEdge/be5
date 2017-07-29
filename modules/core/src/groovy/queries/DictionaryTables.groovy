import com.developmentontheedge.be5.api.Request
import com.developmentontheedge.be5.components.Menu
import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.env.Injector
import com.developmentontheedge.be5.metadata.model.EntityType
import com.developmentontheedge.be5.metadata.model.Query
import com.developmentontheedge.be5.query.TableBuilder

class DictionaryTables implements TableBuilder
{
    @Override
    TableModel get(Query query, Map<String, String> parametersMap, Request req, Injector injector)
    {
        Menu menu = (Menu)injector.getComponent("menu")
        def dictionaryMenu = menu.generateSimpleMenu(injector, EntityType.DICTIONARY)

        List<TableModel.ColumnModel> columns = new ArrayList<>()
        columns.add(new TableModel.ColumnModel("name","Название"))

        List<TableModel.RowModel> rows = new ArrayList<>()
        for (Integer i = 0; i < dictionaryMenu.root.size(); i++) {
            List<TableModel.CellModel> cells = new ArrayList<TableModel.CellModel>()

            def node = dictionaryMenu.root.get(i)
            cells.add(new TableModel.CellModel(node.title,
                    Collections.singletonMap("link", Collections.singletonMap("url", node.action.getArg()))))

            rows.add(new TableModel.RowModel(i.toString(), cells))
        }

        return new TableModel(columns, rows, false, rows.size(), false)
    }
}
