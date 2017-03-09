// $Id: EmptyCacheImplFactory.java,v 1.3 2012/04/06 03:57:06 zha Exp $
package com.developmentontheedge.be5.metadata.caches.impl;

import com.developmentontheedge.be5.metadata.caches.AbstractCacheImpl;
import com.developmentontheedge.be5.metadata.caches.CacheImplFactory;

public class EmptyCacheImplFactory implements CacheImplFactory
{
    /* (non-Javadoc)
     * @see CacheImplFactory#getCacheImpl(java.lang.String)
     */
    public AbstractCacheImpl getCacheImpl(String namespace)
    {
        AbstractCacheImpl ret = new EmptyCacheImpl();
        ret.setNamespace( namespace );
        return ret;
    }

    public AbstractCacheImpl getCacheImpl(String namespace, int size )
    {
        return this.getCacheImpl( namespace );
    }
}
