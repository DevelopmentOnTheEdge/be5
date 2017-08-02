import com.developmentontheedge.be5.api.services.CacheInfo
import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.query.TableSupport
import com.github.benmanes.caffeine.cache.Cache


class CacheTable extends TableSupport
{
    @Override
    TableModel get()
    {
        columns = getColumns("Name",
                "Hit Rate",
                "Eviction Count",
                "Average Load Penalty")

        for (Map.Entry<String, Cache> entry : CacheInfo.caches.entrySet())
        {
            rows.add(getRow(entry.getKey(), getCells(
                    entry.getKey(),
                    String.format("%.4f", entry.getValue().stats().hitRate()),
                    entry.getValue().stats().evictionCount(),
                    entry.getValue().stats().averageLoadPenalty()
            )))
        }

        return getTable(columns, rows)
    }
}
