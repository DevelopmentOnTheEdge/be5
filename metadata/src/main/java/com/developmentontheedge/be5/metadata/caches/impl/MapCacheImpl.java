// $Id: MapCacheImpl.java,v 1.11 2012/03/23 09:18:36 zha Exp $
package com.developmentontheedge.be5.metadata.caches.impl;

import com.developmentontheedge.be5.metadata.caches.AbstractCacheImpl;

import java.util.Map;

/**
 * Simple map-based cache for simple non-distributed cases.
 * 
 * 
 * @author puz
 *
 */
public class MapCacheImpl extends AbstractCacheImpl
{       
    /**
     * Cache following delete-LRU policy.
     */
    private LRUConcurrentStorage lruStorage;

    public MapCacheImpl(final String namespace)
    {
        setNamespace( namespace );
        lruStorage = new LRUConcurrentStorage( getCacheSize() );
    }

    public MapCacheImpl(final String namespace, int cacheSize)
    {
        setNamespace( namespace );
        setCacheSize( cacheSize );
        lruStorage = new LRUConcurrentStorage( getCacheSize() );
    }

    public void clear()
    {
        lruStorage.clear();
    }

    public Object get(Object key)
    {
        updateUsageCount();
        Object ret = lruStorage.get( getNamespaceAwareKey( key ) ); 
        if( ret == null )
        {
            updateMissCount();
        }
        return ret;
    }

    public void put(Object key, Object val)
    {
        lruStorage.put( getNamespaceAwareKey( key ), val );

    }

    public void putAll(Map<Object, Object> values)
    {
        for( final Map.Entry<Object, Object> e : values.entrySet() )
        {
            lruStorage.put( getNamespaceAwareKey( e.getKey() ), e.getValue() );
        }
    }

    public void remove(Object key)
    {
        lruStorage.remove( getNamespaceAwareKey( key ) );
    }

    public int countEntries()
    {
        return lruStorage.size();
    }
}
