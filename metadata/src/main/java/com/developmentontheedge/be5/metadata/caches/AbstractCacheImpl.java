// $Id: AbstractCacheImpl.java,v 1.12 2012/03/23 09:18:36 zha Exp $
package com.developmentontheedge.be5.metadata.caches;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Abstract cache implementation.
 * 
 * @author puz
 *
 */
public abstract class AbstractCacheImpl
{
    public abstract Object get(Object key);

    public abstract void put(Object key, Object val);

    public abstract void putAll(Map<Object, Object> values);

    public abstract void remove(Object key);

    public abstract void clear();

    public abstract int countEntries() throws Exception;

    public void setNamespace(String namespace)
    {
        this.namespace = namespace;
    }

    AtomicLong usageCount = new AtomicLong();
    protected void updateUsageCount()
    {
        usageCount.incrementAndGet();
    }

    public long countUsages()
    {
        return usageCount.longValue();
    }

    public void resetUsages()
    {
        usageCount.set( 0l );
    }

    AtomicLong missCount = new AtomicLong();
    protected void updateMissCount()
    {
        missCount.incrementAndGet();
    }

    public long countMisses()
    {
        return missCount.longValue();
    }

    public void resetMisses()
    {
        missCount.set( 0l );
    }

    public String getNamespace()
    {
        return namespace;
    }

    private String namespace;
    
    /**
     * Default cache size (in objects)
     */
    private int cacheSize = 1000;


    private static final String DELIMITER = "`#$`";
    
    /**
     * Get namespace-aware key.
     * 
     * @param key
     * @return
     */
    protected String getNamespaceAwareKey(final Object key)
    {        
        return key + DELIMITER + namespace;
    }

    public void setCacheSize(int cacheSize)
    {
        this.cacheSize = cacheSize;
    }

    public int getCacheSize()
    {
        return cacheSize;
    }
}
