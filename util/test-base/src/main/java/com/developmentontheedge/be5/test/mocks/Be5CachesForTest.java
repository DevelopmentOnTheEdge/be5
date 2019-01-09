package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.cache.Be5Caches;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Be5CachesForTest implements Be5Caches
{
    private Map<String, Cache> caches = new ConcurrentHashMap<>();

    @Override
    public void registerCache(String name, Cache cache)
    {
        if (caches.containsKey(name)) throw Be5Exception.internal("caches containsKey: " + name);
        caches.put(name, cache);
    }

    @Override
    public <K, V> Cache<K, V> createCache(String name)
    {
        Cache<K, V> newCache = Caffeine.newBuilder()
                .maximumSize(getCacheSize(name))
                .recordStats()
                .build();
        registerCache(name, newCache);
        return newCache;
    }

    @Override
    public Map<String, Cache> getCaches()
    {
        return caches;
    }

    @Override
    public Cache getCache(String name)
    {
        return caches.get(name);
    }

    @Override
    public int getCacheSize(String name)
    {
        return 0;
    }

    @Override
    public void clearAll()
    {

    }
}
