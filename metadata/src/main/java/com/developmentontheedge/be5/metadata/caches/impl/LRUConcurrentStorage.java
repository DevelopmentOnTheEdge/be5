// $Id: LRUConcurrentStorage.java,v 1.2 2014/02/07 09:55:30 lan Exp $
package com.developmentontheedge.be5.metadata.caches.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LRUConcurrentStorage
{
    private int cacheSize = 1000;
    private SyncLinkedHashMap[] maps;
    private final int segmentShift;
    private final int segmentMask;

    public LRUConcurrentStorage(int cacheSize, int segmentCount)
    {
        this.cacheSize = cacheSize;
        int sshift = 0;
        int ssize = 1;
        while (ssize < segmentCount) {
            ++sshift;
            ssize <<= 1;
        }
        this.segmentShift = 32 - sshift;
        this.segmentMask = ssize - 1;
        this.maps = new SyncLinkedHashMap[ssize];
        for( int i = 0; i < maps.length; i++ )
        {
            maps[i] = new SyncLinkedHashMap();
        }
    }

    public LRUConcurrentStorage(int cacheSize)
    {
        this( cacheSize, 16 );
    }

    @SuppressWarnings ( "serial" )
    private class SyncLinkedHashMap extends LinkedHashMap<Object, Object>
    {
        private ReadWriteLock rwLock;
        private Lock readLock;
        private Lock writeLock;

        SyncLinkedHashMap()
        {
            rwLock = new ReentrantReadWriteLock();
            readLock = rwLock.readLock();
            writeLock = rwLock.writeLock();
        }

        protected boolean removeEldestEntry(Map.Entry<Object, Object> eldest)
        {
            return size() > (cacheSize >>> (32-segmentShift));
        }

        @Override
        public Object get(Object key)
        {
            try
            {
                readLock.lock();
                return super.get( key );
            }
            finally
            {
                readLock.unlock();
            }
        }

        @Override
        public Object put(Object key, Object value)
        {
            try
            {
                writeLock.lock();
                return super.put( key, value );
            }
            finally
            {
                writeLock.unlock();
            }
        }

        @Override
        public void putAll(Map<? extends Object, ? extends Object> m)
        {
            try
            {
                writeLock.lock();
                super.putAll( m );
            }
            finally
            {
                writeLock.unlock();
            }
        }

        @Override
        public Object remove(Object key)
        {
            try
            {
                writeLock.lock();
                return super.remove( key );
            }
            finally
            {
                writeLock.unlock();
            }
        }


    }

    private SyncLinkedHashMap getSegment(Object key)
    {
        return maps[(key.hashCode() >>> segmentShift) & segmentMask];
    }

    public Object get(Object key)
    {
        return getSegment( key ).get( key );
    }

    public void put(Object key, Object val)
    {
        getSegment( key ).put( key, val );
    }

    public void putAll(Map<Object, Object> values)
    {
        for( Map.Entry<Object, Object> e : values.entrySet() )
        {
            Object key = e.getKey();
            getSegment( key ).put( key, e.getValue() );
        }
    }

    public void remove(Object key)
    {
        getSegment( key ).remove( key );
    }

    public void clear()
    {
        for( SyncLinkedHashMap m : maps )
        {
            m.clear();
        }
    }

    public int size()
    {
        int c = 0;
        for( SyncLinkedHashMap m : maps )
        {
            c += m.size();
        }
        return c;
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
