// $Id: Cache.java,v 1.9 2012/04/06 03:57:06 zha Exp $
package com.developmentontheedge.be5.metadata.caches;

import com.developmentontheedge.beans.logging.Logger;
import com.developmentontheedge.beans.logging.LoggingHandle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author puz
 *
 */
public class Cache
{
    private static LoggingHandle cat = Logger.getHandle( Cache.class.getName() );

    private AbstractCacheImpl cacheImpl;

    /**
     *
     * @param cacheImpl
     */
    public Cache(AbstractCacheImpl cacheImpl)
    {
        this.cacheImpl = cacheImpl;
    }

    public AbstractCacheImpl getCacheImpl()
    {
        return cacheImpl;
    }

    private List<String> affectingTables = Collections.emptyList();

    public List<String> getAffectingTables()
    {
        return affectingTables;
    }

    public void setAffectingTables( String ... tables )
    {
        affectingTables = new ArrayList<String>();
        Collections.addAll( affectingTables, tables );
    }

    public Object get(Object key)
    {
        try
        {
            return cacheImpl.get( key );
        }
        catch( Exception e )
        {
            Logger.error( cat, "Failed to get object from cache cache with key '" + key + "'", e );
            return null;
        }
    }

    public void put(Object key, Object val)
    {
        try
        {
            cacheImpl.put( key, val );
        }
        catch( Exception e )
        {
            Logger.error( cat, "Failed to put object " + val + " to cache with key '" + key + "'", e );
        }
    }

    public void putAll(Map<Object, Object> values)
    {
        try
        {
            cacheImpl.putAll( values );
        }
        catch( Exception e )
        {
            Logger.error( cat, "Failed to put objects " + values + " to the cache", e );
        }
    }

    public void remove(Object key)
    {
        try
        {
            cacheImpl.remove( key );
        }
        catch( Exception e )
        {
            Logger.error( cat, "Failed to remove object from cache by key '" + key + "'", e );
        }
    }

    public void clear()
    {
        try
        {
            cacheImpl.clear();
            cacheImpl.resetUsages();
            cacheImpl.resetMisses();
        }
        catch( Exception e )
        {
            Logger.error( cat, "Failed to clear cache", e );
        }
    }

    public int countEntries()
    {
        try
        {
            return cacheImpl.countEntries();
        }
        catch( Exception e )
        {
            Logger.error( cat, "Failed to retrieve number of entries in the cache", e );
            return -1;
        }
    }
}
