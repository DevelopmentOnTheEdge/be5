import com.developmentontheedge.be5.components.Menu
import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.components.impl.model.TableModel.CellModel
import com.developmentontheedge.be5.metadata.model.EntityType
import com.developmentontheedge.be5.query.TableSupport

class DictionaryTables extends TableSupport
{
    @Override
    TableModel get()
    {
        Menu menu = (Menu)injector.getComponent("menu")
        def dictionaryMenu = menu.generateSimpleMenu(injector, EntityType.DICTIONARY)

        columns = getColumns("name")

        for (Integer i = 0; i < dictionaryMenu.root.size(); i++) {
            List<CellModel> cells = new ArrayList<CellModel>()

            def node = dictionaryMenu.root.get(i)
            cells.add(new CellModel(node.title,
                    Collections.singletonMap("link", Collections.singletonMap("url", node.action.getArg()))))

            rows.add(getRow(i.toString(), cells))
        }

        return getTable(columns, rows)
    }
}
