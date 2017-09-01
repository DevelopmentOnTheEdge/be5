import com.developmentontheedge.be5.api.services.Be5Caches
import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.query.TableBuilderSupport
import com.github.benmanes.caffeine.cache.Cache


class CacheTable extends TableBuilderSupport
{
    @Inject Be5Caches be5Caches

    @Override
    TableModel getTable()
    {
        addColumns("Name",
                "Hit rate",
                "Eviction count",
                "Average load penalty",
                "Load / hit count",
                "Failure count")

        for (Map.Entry<String, Cache> entry : be5Caches.caches.entrySet())
        {
            addRow(entry.getKey(), cells(
                    entry.getKey(),
                    String.format("%.4f", entry.getValue().stats().hitRate()),
                    entry.getValue().stats().evictionCount(),
                    String.format("%.4f", entry.getValue().stats().averageLoadPenalty() / 1000_000_000.0),
                    entry.getValue().stats().loadCount() + " / " + entry.getValue().stats().hitCount(),
                    entry.getValue().stats().loadFailureCount()
            ))
        }

        return table(columns, rows)
    }
}
