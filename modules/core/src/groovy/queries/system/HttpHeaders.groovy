package system

import com.developmentontheedge.be5.query.model.TableModel
import com.developmentontheedge.be5.queries.support.TableBuilderSupport


class HttpHeaders extends TableBuilderSupport
{
    @Override
    TableModel getTableModel()
    {
        addColumns("name", "value")

        Enumeration<String> keys = request.rawRequest.getHeaderNames();
        if (keys != null) {
            while (keys.hasMoreElements()) {
                String key = (String)keys.nextElement()

                addRow(cells(
                        key,
                        request.rawRequest.getHeader(key)
                ))

            }
        }
        return table(columns, rows)
    }
}
