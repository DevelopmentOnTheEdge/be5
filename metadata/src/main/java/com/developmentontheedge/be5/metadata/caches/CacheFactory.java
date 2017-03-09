// $Id: CacheFactory.java,v 1.24 2014/04/10 09:40:12 zha Exp $
package com.developmentontheedge.be5.metadata.caches;

import com.developmentontheedge.be5.metadata.caches.impl.EmptyCacheImpl;
import com.developmentontheedge.be5.metadata.caches.impl.MapCacheImplFactory;
import com.developmentontheedge.be5.metadata.util.StringUtils;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.developmentontheedge.beans.logging.Logger;
import com.developmentontheedge.beans.logging.LoggingHandle;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;


/**
 * @author puz
 */
public class CacheFactory
{
    private static LoggingHandle cat = Logger.getHandle( "CacheFactory" );

    public static final String CACHE_IMPL_FACTORY_CLASS_NAME_PARAM = "cacheImplementationFactory";

    /**
     * By-default use map implementation.
     */
    public static final CacheImplFactory DEFAULT_CACHE_IMPL_FACTORY = new MapCacheImplFactory();

    private CacheFactory()
    {
    }

    private static Map<String, Cache> caches = new HashMap<String, Cache>();
    private static Map<String, Integer> cacheSizes = new HashMap<String, Integer>();

    private static List<String> disabledCaches = Collections.EMPTY_LIST;

    public static void setDisabledCachesFromString( String dis )
    {
        Logger.info( cat, "Processing DISABLED_CACHES = " + dis );
        List<String> newList = Collections.EMPTY_LIST;

        if( !StringUtils.isEmpty(dis) )
        {
            newList = Arrays.asList( dis.split( "," ) );
        }

        List<String> changed = new ArrayList<String>();
        for( int i = 0; i < newList.size(); i++ )
        {
            if( !disabledCaches.contains( newList.get( i ) ) )
                changed.add( newList.get( i ) );
        }
        for( int i = 0; i < disabledCaches.size(); i++ )
        {
            if( !newList.contains( disabledCaches.get( i ) ) )
                changed.add( disabledCaches.get( i ) );
        }

        List<String> toRemove = new ArrayList<String>();

        for( Map.Entry<String, Cache> e : caches.entrySet() )
        {
            if( changed.contains( e.getValue().getCacheImpl().getNamespace() ) )
                toRemove.add( e.getKey() );
        }

        for( int i = 0; i < toRemove.size(); i++ )
        {
            String key = toRemove.get( i );
            Logger.info( cat, "Cache \"" + key + "\" is to be re-initialized because DISABLED_CACHES has changed" );
            caches.get( key ).clear();
            caches.remove( key );
        }

        disabledCaches = newList;
    }

    public static void setCacheSizesFromString( String sizes )
    {
        for( String size : sizes.split( "," ) )
        {
            try
            {
                String []pair = size.split( "=" );
                cacheSizes.put( pair[ 0 ], Integer.parseInt( pair[ 1 ] ) );
            }
            catch( Exception exc )
            {
                Logger.error( cat, "Unable to set cache size from '" + size + "'" );
            }
        }
    }

    public static String clearCachesFromString( String cachesToClear )
    {
        List<String> toClear = Arrays.asList( cachesToClear.split( "," ) );
        ArrayList<String> cleared = new ArrayList<String>();
        for( Cache c : caches.values() )
        {
            String cname = c.getCacheImpl().getNamespace();
            if( toClear.contains( cname ) )
            {
                c.clear();
                cleared.add( cname );
                continue; 
            }
            if( toClear.contains( "all" ) )
            {
                c.clear();
                cleared.add( cname );
                continue; 
            }
            if( toClear.contains( "metadata_*" ) && cname.startsWith( "metadata_" ) )
            {
                c.clear();
                cleared.add( cname );
                continue; 
            }
        }
        return cleared.toString();
    }

    public static List<String> getNamespaces()
    {
        ArrayList<String> ret = new ArrayList<String>();
        for( Cache c : caches.values() )
        {
            ret.add( c.getCacheImpl().getNamespace() );
        }
        return ret;
    }

    public static boolean clearByNamespace( String namespace )
    {
        for( Cache c : caches.values() )
        {
            if( namespace.equals( c.getCacheImpl().getNamespace() ) )
            {
                c.clear();
                return true;
            }
        }
        return false;
    }

    public static boolean clearByAffectingTable( String table )
    {
        boolean bCleared = false;
        for( Cache c : caches.values() )
        {
            if( c.getAffectingTables().contains( table ) )
            {
                c.clear();
                bCleared = true;
            }
        }
        return bCleared;
    }

    public static List<DynamicPropertySet> listCaches()
    {
        List<DynamicPropertySet> rows = new ArrayList<DynamicPropertySet>();
        for( Cache c : caches.values() )
        {
            DynamicPropertySet bean = new DynamicPropertySetSupport();
            bean.add( new DynamicProperty( "Cache", String.class, c.getCacheImpl().getNamespace() ) );

            try
            {
                bean.add( new DynamicProperty( "Entries", Integer.class, c.getCacheImpl().countEntries() ) );
            }
            catch( Exception e )
            {
                StringWriter out = new StringWriter();
                e.printStackTrace( new PrintWriter( out ) );
                String entries = "<pre>" + out.toString() + "/<pre>";
                bean.add( new DynamicProperty( "Entries", String.class, entries ) );
            }

            try
            {
                bean.add( new DynamicProperty( "Usages", Integer.class, c.getCacheImpl().countUsages() ) );
            }
            catch( Exception e )
            {
                StringWriter out = new StringWriter();
                e.printStackTrace( new PrintWriter( out ) );
                String usages = "<pre>" + out.toString() + "/<pre>";
                bean.add( new DynamicProperty( "Usages", String.class, usages ) );
            }

            try
            {
                bean.add( new DynamicProperty( "Misses", Integer.class, c.getCacheImpl().countMisses() ) );
            }
            catch( Exception e )
            {
                StringWriter out = new StringWriter();
                e.printStackTrace( new PrintWriter( out ) );
                String misses = "<pre>" + out.toString() + "/<pre>";
                bean.add( new DynamicProperty( "Misses", String.class, misses ) );
            }

            rows.add( bean );
        }
        return rows;
    }

    /**
     *
     */
    public static void clearAllCaches()
    {
        for( Cache c : caches.values() )
        {
            c.clear();
        }
        //caches.clear();
    }

    /**
     * Create unique cache instance for each namespace + implementation type.
     *
     * @param namespace
     * @param cacheImplFactory
     * @return
     */
    public static synchronized Cache getCacheInstance( String namespace, CacheImplFactory cacheImplFactory, Class cacheClass )
    {
        final String key = cacheImplFactory == null ? namespace : ( namespace + cacheImplFactory.getClass() );

        Cache cache = caches.get( key );
        if( cache == null )
        {
            AbstractCacheImpl impl;
            if( disabledCaches.contains( namespace ) )
            {
                Logger.info( cat, "Cache \"" + namespace + "\" is disabled. Using EmptyCacheImpl" );
                impl = new EmptyCacheImpl();
                impl.setNamespace( namespace );
            }
            else
            {
                Integer cacheSize = cacheSizes.get( namespace );
                if( cacheSize != null )
                {
                    impl = cacheImplFactory.getCacheImpl( namespace, cacheSize );
                    Logger.info( cat, "Cache \"" + namespace + "\" created with custom size " + cacheSize );
                }
                else
                {
                    impl = cacheImplFactory.getCacheImpl( namespace );
                }
            }

            if( cacheClass != null )
            {
                try
                {
                   cache = ( Cache )cacheClass.getConstructor( AbstractCacheImpl.class ).newInstance( impl );
                }
                catch( Exception exc )
                {
                    throw new RuntimeException( exc );
                }
            }
            else
            {
                cache = new Cache( impl );
            }

            caches.put( key, cache );
        }

        return cache;
    }


    /**
     * Create cache with implementation set in web.xml.
     *
     * @param namespace
     * @return
     */
    public static Cache getCacheInstance( String namespace )
    {
        return getCacheInstance( namespace, getCacheImplFactory(), null );
    }


    /**
     * Create application-wide cache implementation factory.
     *
     * @return
     */
    public synchronized static CacheImplFactory getCacheImplFactory()
    {
        if( applicationCacheImplFactory == null )
        {
//TODO            final String className = WebAppInitializer.getWebXMLParameter( CACHE_IMPL_FACTORY_CLASS_NAME_PARAM );
//            try
//            {
//                applicationCacheImplFactory = ( CacheImplFactory )Class.forName( className ).newInstance();
//            }
//            catch( Throwable t )
//            {
                applicationCacheImplFactory = DEFAULT_CACHE_IMPL_FACTORY;
            //}
        }
        return applicationCacheImplFactory;
    }

    public synchronized static void setCacheImplFactory(CacheImplFactory cacheImplFactory)
    {
        applicationCacheImplFactory = cacheImplFactory;
    }

    private static CacheImplFactory applicationCacheImplFactory;
}
