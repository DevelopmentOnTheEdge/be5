// $Id: JCSCacheImpl.java,v 1.9 2012/04/06 03:57:06 zha Exp $
package com.developmentontheedge.be5.metadata.caches.impl;

import com.developmentontheedge.beans.logging.Logger;
import com.developmentontheedge.beans.logging.LoggingHandle;
import com.developmentontheedge.be5.metadata.caches.CacheWithStatistics;
import com.developmentontheedge.be5.metadata.Utils;
import com.developmentontheedge.be5.metadata.caches.AbstractCacheImpl;
import org.apache.jcs.JCS;
import org.apache.jcs.access.CacheAccess;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.stats.behavior.ICacheStats;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Cache implemented through JCS library.
 * 
 * @author puz
 *
 */
public class JCSCacheImpl extends AbstractCacheImpl implements CacheWithStatistics
{
    private static LoggingHandle cat = Logger.getHandle( JCSCacheImpl.class.getName() );

    private JCS jcs;

    private static Properties defaultProps = new Properties();

    static
    {
        defaultProps.put( "jcs.default", "" );
        defaultProps.put( "jcs.default.cacheattributes.MemoryCacheName", "org.apache.jcs.engine.memory.lru.LRUMemoryCache" );
        defaultProps.put( "jcs.default.cacheattributes", "org.apache.jcs.engine.CompositeCacheAttributes" );
        defaultProps.put( "jcs.default.cacheattributes.MaxObjects", "2000" );
    }

    public JCSCacheImpl(final String namespace)
    {
        setNamespace( namespace );
        try
        {
            jcs = JCS.getInstance( namespace );
        }
        catch( IllegalStateException e1 )
        {
            System.out.println( namespace + ":\n" + Utils.trimStackAsString( e1 ) );
            // cache.ccf file was not found (this may happen when one launch tests from IDE for example)
            // and default properties have to be set explicitly
            // Logger.warn( cat, e1.getMessage() + " Going to use default properties" );
            CompositeCacheManager ccm = CompositeCacheManager.getUnconfiguredInstance();
            ccm.configure( defaultProps );
            try
            {
                jcs = JCS.getInstance( namespace );
            }
            catch( Exception e2 )
            {
                Logger.error( cat, "Cache initialization failed!", e2 );
                System.out.println( "Cache initialization failed!\n" + Utils.trimStackAsString( e2 ) );
            }
        }
        catch( Exception e2 )
        {
            Logger.error( cat, "Cache initialization failed!", e2 );
            System.out.println( "Cache initialization failed!\n" + Utils.trimStackAsString( e2 ) );
        }
    }

    public Iterator<?> getCachedObjectsIterator() throws Exception
    {
        Field cacheControlField = CacheAccess.class.getDeclaredField( "cacheControl" );
        cacheControlField.setAccessible( true );
        CompositeCache cc = (CompositeCache)cacheControlField.get( jcs );
        return cc.getMemoryCache().getIterator();
    }

    public List<CacheStatSection> getStatistics() throws Exception
    {
        List<CacheStatSection> result = new ArrayList<CacheStatSection>();
        ICacheStats stats = jcs.getStatistics();
        CacheStatSection statSectionMain = new CacheStatSection();
        IStatElement[] statElements = stats.getStatElements();
        for( int i = 0; i < statElements.length; i++ )
        {
            IStatElement statElement = statElements[i];
            statSectionMain.addStats( statElement.getName(), statElement.getData() );
        }
        result.add( statSectionMain );

        IStats[] auxCacheStats = stats.getAuxiliaryCacheStats();
        for( int i = 0; i < auxCacheStats.length; i++ )
        {
            IStats stat = auxCacheStats[i];
            CacheStatSection statSectionAux = new CacheStatSection();
            statSectionAux.setSectionName( stat.getTypeName() );
            statElements = stat.getStatElements();
            for( int j = 0; j < statElements.length; j++ )
            {
                IStatElement statElement = statElements[j];
                statSectionAux.addStats( statElement.getName(), statElement.getData() );
            }
            result.add( statSectionAux );
        }
        return result;
    }

    public Object get(Object key)
    {
        updateUsageCount();
        Object ret = jcs.get( key ); 
        if( ret == null )
        {
            updateMissCount();
        }
        return ret;
    }

    public void put(Object key, Object val)
    {
        try
        {
            jcs.put( key, val );
        }
        catch( CacheException e )
        {
            Logger.error( cat, "Failed to put object " + val + " to cache with key '" + key + "'", e );
        }
    }

    public void putAll(Map<Object, Object> values)
    {
        Object key = null;
        Object val = null;
        try
        {
            for( Map.Entry<Object, Object> entry : values.entrySet() )
            {
                key = entry.getKey();
                val = entry.getValue();
                jcs.put( key, val );
            }
        }
        catch( CacheException e )
        {
            Logger.error( cat, "Failed to put object " + val + " to cache with key '" + key + "'", e );
        }
    }

    public void remove(Object key)
    {
        try
        {
            jcs.remove( key );
        }
        catch( CacheException e )
        {
            Logger.error( cat, "Failed to remove object from cache by key '" + key + "'", e );
        }
    }


    public void clear()
    {
        try
        {
            jcs.clear();
        }
        catch( CacheException e )
        {
            Logger.error( cat, "Failed to clear cache for region '" + getNamespace() + "'", e );
        }
    }

    public int countEntries() throws Exception
    {
        Field cacheControlField = CacheAccess.class.getDeclaredField( "cacheControl" );
        cacheControlField.setAccessible( true );
        CompositeCache cc = (CompositeCache)cacheControlField.get( jcs );
        return cc.getSize();
    }
    
}
