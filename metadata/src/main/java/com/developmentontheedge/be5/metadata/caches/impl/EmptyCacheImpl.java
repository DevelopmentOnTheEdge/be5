// $Id: EmptyCacheImpl.java,v 1.6 2012/03/23 09:18:36 zha Exp $
package com.developmentontheedge.be5.metadata.caches.impl;

import com.developmentontheedge.be5.metadata.caches.AbstractCacheImpl;

import java.util.Map;

/**
 * Cache that is always empty.
 * Used to force reading of the settings from DB.
 * 
 * @author puz
 *
 */
public class EmptyCacheImpl extends AbstractCacheImpl
{
    public void clear()
    {
    }

    public Object get(Object key)
    {
        updateUsageCount();
        updateMissCount();
        return null;
    }

    public void put(Object key, Object val)
    {
    }

    public void putAll(Map<Object, Object> values)
    {
    }

    public void remove(Object key)
    {
    }

    public int countEntries()
    {
        return 0;
    }
}
