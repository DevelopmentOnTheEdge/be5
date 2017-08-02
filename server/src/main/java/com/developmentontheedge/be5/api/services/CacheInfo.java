package com.developmentontheedge.be5.api.services;

import com.github.benmanes.caffeine.cache.Cache;

import java.util.HashMap;
import java.util.Map;

public class CacheInfo
{
    private static Map<String, Cache> caches = new HashMap<>();

    public static void registerCache(String name, Cache cache)
    {
        caches.put(name, cache);
    }

    public static Map<String, Cache> getCaches()
    {
        return caches;
    }

    public static void clearAll()
    {
        caches.forEach((k,v) -> v.invalidateAll());
    }
}
