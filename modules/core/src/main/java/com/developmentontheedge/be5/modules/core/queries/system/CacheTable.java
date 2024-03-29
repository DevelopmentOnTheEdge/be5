package com.developmentontheedge.be5.modules.core.queries.system;

import com.developmentontheedge.be5.cache.Be5Caches;
import com.developmentontheedge.be5.database.QRec;
import com.developmentontheedge.be5.server.queries.support.QueryExecutorSupport;
import com.github.benmanes.caffeine.cache.Cache;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;


public class CacheTable extends QueryExecutorSupport
{
    @Inject
    private Be5Caches be5Caches;

    @Override
    public List<QRec> execute()
    {
        addColumns("Name",
                "Size",
                "Hit rate",
                "Eviction count",
                "Average load penalty",
                "Load / hit count",
                "Miss count",
                "Failure count");

        for (Map.Entry<String, Cache> entry : be5Caches.getCaches().entrySet())
        {
            addRow(entry.getKey(), cells(
                    entry.getKey(),
                    be5Caches.getCacheSize(entry.getKey()),
                    String.format("%.4f", entry.getValue().stats().hitRate()),
                    entry.getValue().stats().evictionCount(),
                    String.format("%.4f", entry.getValue().stats().averageLoadPenalty() / 1000_000_000.0),
                    entry.getValue().stats().loadCount() + " / " + entry.getValue().stats().hitCount(),
                    entry.getValue().stats().missCount(),
                    entry.getValue().stats().loadFailureCount()
            ));
        }
        return table();
    }
}
