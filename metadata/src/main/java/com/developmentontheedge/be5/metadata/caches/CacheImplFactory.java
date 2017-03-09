// $Id: CacheImplFactory.java,v 1.3 2012/04/06 03:57:06 zha Exp $
package com.developmentontheedge.be5.metadata.caches;


/**
 * Generic factory for creating cache-implementations 
 * (JCS-based, simple map-based, etc.).
 * 
 * @author puz
 *
 */
public interface CacheImplFactory
{
    AbstractCacheImpl getCacheImpl(String namespace);

    AbstractCacheImpl getCacheImpl(String namespace, int size);
}