package com.developmentontheedge.be5.api.services;

import com.github.benmanes.caffeine.cache.Cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class CacheInfo
{
    private static Map<String, Cache> caches = new ConcurrentHashMap<>();

    public static void registerCache(String name, Cache cache)
    {
        caches.put(name, cache);
    }

    public static Map<String, Cache> getCaches()
    {
        return caches;
    }

    public static Cache getCache(String name)
    {
        return caches.get(name);
    }

    public static void clearAll()
    {
        caches.forEach((k,v) -> v.invalidateAll());
    }
}
