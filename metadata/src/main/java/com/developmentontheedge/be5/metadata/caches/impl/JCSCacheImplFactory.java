package com.developmentontheedge.be5.metadata.caches.impl;

import com.developmentontheedge.be5.metadata.caches.AbstractCacheImpl;
import com.developmentontheedge.be5.metadata.caches.CacheImplFactory;

/**
 * 
 * @author puz
 *
 */
public class JCSCacheImplFactory implements CacheImplFactory
{
    /* (non-Javadoc)
     * @see CacheImplFactory#getCacheImpl(java.lang.String)
     */
    public AbstractCacheImpl getCacheImpl(String namespace)
    {
        return new JCSCacheImpl( namespace );
    }

    public AbstractCacheImpl getCacheImpl(String namespace, int size )
    {
        return this.getCacheImpl( namespace );
    }
}