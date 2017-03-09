/** $Id: AbstractCache.java,v 1.15 2012/04/03 04:34:20 zha Exp $ */

package com.developmentontheedge.be5.metadata.caches;

import com.developmentontheedge.beans.logging.Logger;
import com.developmentontheedge.beans.logging.LoggingHandle;
import com.developmentontheedge.be5.metadata.Utils;
import org.apache.jcs.JCS;
import org.apache.jcs.access.CacheAccess;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.stats.behavior.ICacheStats;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @deprecated Use Cache with appropriate implementation instead.
 * 
 * @author puz
 *
 */
public abstract class AbstractCache
{
    private static LoggingHandle cat = Logger.getHandle( AbstractCache.class.getName() );
    protected JCS cache;
    private String regionName;
    private static Properties defaultProps = new Properties();

    static
    {
        defaultProps.put( "jcs.default", "" );
        defaultProps.put( "jcs.default.cacheattributes.MemoryCacheName", "org.apache.jcs.engine.memory.lru.LRUMemoryCache" );
        defaultProps.put( "jcs.default.cacheattributes", "org.apache.jcs.engine.CompositeCacheAttributes" );
        defaultProps.put( "jcs.default.cacheattributes.MaxObjects", "2000" );
    }

    protected AbstractCache()
    {
        try
        {
            this.regionName = getRegionName();
            cache = JCS.getInstance( regionName );
        }
        catch( IllegalStateException e1 )
        {
            System.out.println( getRegionName() + ":\n" + Utils.trimStackAsString( e1 ) );
            // cache.ccf file was not found (this may happen when one launch tests from IDE for example)
            // and default properties have to be set explicitly
            // Logger.warn( cat, e1.getMessage() + " Going to use default properties" );
            CompositeCacheManager ccm = CompositeCacheManager.getUnconfiguredInstance();
            ccm.configure( defaultProps );
            try
            {
                cache = JCS.getInstance( regionName );
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

    protected abstract String getRegionName();

    public Iterator getCachedObjectsIterator() throws Exception
    {
        Field cacheControlField = CacheAccess.class.getDeclaredField( "cacheControl" );
        cacheControlField.setAccessible( true );
        CompositeCache cc = ( CompositeCache )cacheControlField.get( cache );
        return cc.getMemoryCache().getIterator();
    }

    public List getStatistics() throws NoSuchFieldException
    {
        List result = new ArrayList();
        ICacheStats stats = cache.getStatistics();
        CacheStatSection statSectionMain = new CacheStatSection();
        IStatElement[] statElements = stats.getStatElements();
        for( int i = 0; i < statElements.length; i++ )
        {
            IStatElement statElement = statElements[ i ];
            statSectionMain.addStats( statElement.getName(), statElement.getData() );
        }
        result.add( statSectionMain );

        IStats[] auxCacheStats = stats.getAuxiliaryCacheStats();
        for( int i = 0; i < auxCacheStats.length; i++ )
        {
            IStats stat = auxCacheStats[ i ];
            CacheStatSection statSectionAux = new CacheStatSection();
            statSectionAux.setSectionName( stat.getTypeName() );
            statElements = stat.getStatElements();
            for( int j = 0; j < statElements.length; j++ )
            {
                IStatElement statElement = statElements[ j ];
                statSectionAux.addStats( statElement.getName(), statElement.getData() );
            }
            result.add( statSectionAux );
        }
        return result;
    }

    public Object get( String key )
    {
        //String realKey = regionName + key;
        String realKey = key;
        try
        {
            Object result = cache.get( realKey );
            //if( Logger.isDebugEnabled( cat ) )
            //{
            //    Logger.debug( cat, "Successfully got object " + result + " from cache by key '" + realKey + "'" );
            //}
            return result;
        }
        catch( Exception e )
        {
            Logger.error( cat, "Failed to get object from cache cache with key '" + realKey + "'", e );
            return null;
        }
    }

    public void put( String key, Object val )
    {
        //String realKey = regionName + key;
        String realKey = key;
        try
        {
            //cache.remove( realKey );
            cache.put( realKey, val );
            //if( Logger.isDebugEnabled( cat ) )
            //{
            //    Logger.debug( cat, "Successfully put object " + val + " to cache with key '" + realKey + "'" );
            //}
        }
        catch( Exception e )
        {
            Logger.error( cat, "Failed to put object " + val + " to cache with key '" + realKey + "'", e );
        }
    }

    public void putAll( Map values )
    {
        String realKey = null;
        Object val = null;
        try
        {
            for( Iterator entries = values.entrySet().iterator(); entries.hasNext(); )
            {
                Map.Entry entry = ( Map.Entry )entries.next();
                realKey = ( String )entry.getKey();
                val = entry.getValue();
                cache.put( realKey, val );
            }
        }
        catch( Exception e )
        {
            Logger.error( cat, "Failed to put object " + val + " to cache with key '" + realKey + "'", e );
        }
    }

    public void remove( String key )
    {
        //String realKey = regionName + key;
        String realKey = key;
        try
        {
            cache.remove( realKey );
            //if( Logger.isDebugEnabled( cat ) )
            //{
            //    Logger.debug( cat, "Successfully removed object from cache by key '" + realKey + "'" );
            //}
        }
        catch( Exception e )
        {
            Logger.error( cat, "Failed to remove object from cache by key '" + realKey + "'", e );
        }
    }

    public void clear()
    {
        try
        {
            cache.clear();
        }
        catch( Exception e )
        {
            Logger.error( cat, "Failed to clear cache for region '" + regionName + "'", e );
        }
    }

    public static class CacheStatSection
    {
        private String sectionName;
        private final Map stats;

        public CacheStatSection()
        {
            stats = new HashMap();
        }

        public String getSectionName()
        {
            return sectionName;
        }

        public void setSectionName( String sectionName )
        {
            this.sectionName = sectionName;
        }

        public Map getStats()
        {
            return stats;
        }

        public void addStats( String name, String value )
        {
            stats.put( name, value );
        }
    }
}