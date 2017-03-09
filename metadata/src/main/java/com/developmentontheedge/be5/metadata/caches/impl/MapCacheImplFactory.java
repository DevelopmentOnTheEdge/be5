// $Id: MapCacheImplFactory.java,v 1.2 2012/04/06 03:57:06 zha Exp $
package com.developmentontheedge.be5.metadata.caches.impl;

import com.developmentontheedge.be5.metadata.caches.AbstractCacheImpl;
import com.developmentontheedge.be5.metadata.caches.CacheImplFactory;

/**
 * 
 * @author puz
 *
 */
public class MapCacheImplFactory implements CacheImplFactory
{
    public AbstractCacheImpl getCacheImpl(String namespace)
    {
        return new MapCacheImpl( namespace );
    }

    public AbstractCacheImpl getCacheImpl( String namespace, int size )
    {
        return new MapCacheImpl( namespace, size );
    }
}
