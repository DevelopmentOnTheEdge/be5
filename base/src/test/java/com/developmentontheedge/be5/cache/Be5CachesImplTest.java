package com.developmentontheedge.be5.cache;

import com.developmentontheedge.be5.BaseTest;
import com.developmentontheedge.be5.config.ConfigurationProvider;
import com.developmentontheedge.be5.meta.ProjectProvider;
import com.github.benmanes.caffeine.cache.Cache;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

public class Be5CachesImplTest extends BaseTest
{
    @Inject private ConfigurationProvider configurationProvider;
    @Inject private ProjectProvider projectProvider;

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
