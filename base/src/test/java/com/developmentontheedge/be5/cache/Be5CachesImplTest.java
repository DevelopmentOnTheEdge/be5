package com.developmentontheedge.be5.base.cache;

import com.developmentontheedge.be5.base.BaseTest;
import com.developmentontheedge.be5.base.config.ConfigurationProvider;
import com.developmentontheedge.be5.base.meta.ProjectProvider;
import com.github.benmanes.caffeine.cache.Cache;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

public class Be5CachesImplTest extends BaseTest
{
    @Inject ConfigurationProvider configurationProvider;
    @Inject ProjectProvider projectProvider;

    @Test
    public void clearAll()
    {
        Be5CachesImpl be5Caches = new Be5CachesImpl(configurationProvider, projectProvider);
        Cache<Object, Object> test = be5Caches.createCache("Test");
        assertEquals(1000, be5Caches.getCacheSize("Test"));
        assertEquals(0, be5Caches.getCacheSize("User settings"));
        assertEquals(test, be5Caches.getCache("Test"));
        be5Caches.clearAll();
        assertEquals(1, be5Caches.getCaches().size());
    }
}
