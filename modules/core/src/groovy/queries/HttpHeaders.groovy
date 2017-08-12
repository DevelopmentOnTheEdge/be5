import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.query.TableBuilderSupport


class HttpHeaders extends TableBuilderSupport
{
    @Override
    TableModel getTable()
    {
        addColumns("name", "value")

        Enumeration<String> keys = req.rawRequest.getHeaderNames();
        if (keys != null) {
            while (keys.hasMoreElements()) {
                String key = (String)keys.nextElement()

                addRow(cells(
                        key,
                        req.rawRequest.getHeader(key)
                ))

            }
        }
        return table(columns, rows)
    }
}
