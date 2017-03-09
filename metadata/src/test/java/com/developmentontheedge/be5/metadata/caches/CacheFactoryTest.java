// $Id: CacheFactoryTest.java,v 1.2 2009/01/14 09:45:13 puz Exp $
package com.developmentontheedge.be5.metadata.caches;

import junit.framework.TestCase;

/**
 * 
 * @author puz
 *
 */
public class CacheFactoryTest extends TestCase
{

    public void testGetCacheDefaultInstance()
    {
        Cache cache = CacheFactory.getCacheInstance( "testCache1" );
        assertNotNull( cache );

        assertNotNull( cache.getCacheImpl() );

        Class defaultImplType = CacheFactory.DEFAULT_CACHE_IMPL_FACTORY.getCacheImpl( "testCache" ).getClass();
        Class actualImplType = cache.getCacheImpl().getClass();

        assertEquals( defaultImplType, actualImplType );
    }

    public void testGetCacheInstance() throws Exception
    {
        // reset static singleton variable
//TODO        Field declaredField = CacheFactory.class.getDeclaredField( "applicationCacheImplFactory" );
//        declaredField.setAccessible( true );
//        declaredField.set( null, null );
//
//        WebAppInitializer.setWebXMLParameter( CacheFactory.CACHE_IMPL_FACTORY_CLASS_NAME_PARAM, MapCacheImplFactory.class.getName() );
//
//        Cache cache = CacheFactory.getCacheInstance( "testCache2" );
//        assertNotNull( cache );
//
//        assertNotNull( cache.getCacheImpl() );
//
//        assertEquals( MapCacheImpl.class, cache.getCacheImpl().getClass() );
    }


}
