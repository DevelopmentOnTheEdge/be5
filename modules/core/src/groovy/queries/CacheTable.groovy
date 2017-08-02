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
                "Hit rate",
                "Eviction count",
                "Average load penalty",
                "Load / hit count",
                "Failure count")

        for (Map.Entry<String, Cache> entry : CacheInfo.caches.entrySet())
        {
            rows.add(getRow(entry.getKey(), getCells(
                    entry.getKey(),
                    String.format("%.4f", entry.getValue().stats().hitRate()),
                    entry.getValue().stats().evictionCount(),
                    String.format("%.4f", entry.getValue().stats().averageLoadPenalty() / 1000000000.0),
                    entry.getValue().stats().loadCount() + " / " + entry.getValue().stats().hitCount(),
                    entry.getValue().stats().loadFailureCount()
            )))
        }

        return getTable(columns, rows)
    }
}
