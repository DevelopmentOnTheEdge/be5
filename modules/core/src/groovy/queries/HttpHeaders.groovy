import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.query.TableSupport


class HttpHeaders extends TableSupport
{
    @Override
    TableModel get()
    {
        columns = getColumns("name", "value")

        Enumeration<String> keys = req.rawRequest.getHeaderNames();
        if (keys != null) {
            while (keys.hasMoreElements()) {
                String key = (String)keys.nextElement()

                rows.add(getRow(getCells(
                        key,
                        req.rawRequest.getHeader(key)
                )))

            }
        }
        return getTable(columns, rows)
    }
}
