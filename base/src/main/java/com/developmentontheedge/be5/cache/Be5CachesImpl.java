package com.developmentontheedge.be5.cache;

import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.config.Configurable;
import com.developmentontheedge.be5.meta.ProjectProvider;
import com.developmentontheedge.be5.config.ConfigurationProvider;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Be5CachesImpl implements Be5Caches, Configurable<Be5CachesImpl.Config>
{
    private final Config config;

    public static class Config
    {
        Map<String, Integer> cacheSizes = new HashMap<>();

        public void setCacheSizes(Map<String, Integer> cacheSizes)
        {
            this.cacheSizes = cacheSizes;
        }
    }

    private Map<String, Cache> caches = new ConcurrentHashMap<>();

    @Inject
    public Be5CachesImpl(ConfigurationProvider configurationProvider, ProjectProvider projectProvider)
    {
        //todo move configure to Bootstrap
        config = (Config) configurationProvider.configure(this);

        projectProvider.addToReload(this::clearAll);
    }

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
    public Cache getCache( String name )
    {
        Cache c = caches.get( name );
        /*
        if( c == null )
        {
            synchronized( Be5CachesImpl.class )
            { 
                c = caches.get( name ); 
                if( c == null )
                {
                    return createCache( name );
                }
            }
        }    
        */
        return c;
    }

    @Override
    public void clearAll()
    {
        caches.forEach((k, v) -> v.invalidateAll());
    }

    @Override
    public int getCacheSize(String name)
    {
        if (config != null)
        {
            return config.cacheSizes.getOrDefault(name, config.cacheSizes.getOrDefault("defaultSize", 1000));
        }
        else
        {
            return 1000;
        }
    }
}
