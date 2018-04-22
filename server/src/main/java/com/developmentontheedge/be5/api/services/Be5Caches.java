package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Be5Caches
{
    private Map<String, Cache> caches = new ConcurrentHashMap<>();

    private Be5MainSettings be5MainSettings;

    public Be5Caches(Be5MainSettings be5MainSettings, ProjectProvider projectProvider)
    {
        this.be5MainSettings = be5MainSettings;

        projectProvider.addToReload(this::clearAll);
    }

    public void registerCache(String name, Cache cache)
    {
        if(caches.containsKey(name))throw Be5Exception.internal("caches containsKey: " + name);
        caches.put(name, cache);
    }

    public <K, V> Cache<K, V> createCache(String name)
    {
        Cache<K, V> newCache = Caffeine.newBuilder()
                .maximumSize(be5MainSettings.getCacheSize(name))
                .recordStats()
                .build();
        registerCache(name, newCache);
        return newCache;
    }

    public Map<String, Cache> getCaches()
    {
        return caches;
    }

    public Cache getCache(String name)
    {
        return caches.get(name);
    }

    public void clearAll()
    {
        caches.forEach((k,v) -> v.invalidateAll());
    }
}
