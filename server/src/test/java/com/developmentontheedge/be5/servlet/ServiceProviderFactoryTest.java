package com.developmentontheedge.be5.servlet;

import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.impl.MainServiceProvider;
import com.developmentontheedge.be5.components.Menu;
import com.developmentontheedge.be5.env.ServiceLoader;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;

public class ServiceProviderFactoryTest
{
    private ServiceProvider serviceProvider = new MainServiceProvider();
    private Map<String, Class<?>> loadedClasses = new ConcurrentHashMap<>();

    @Before
    public void load() throws IOException
    {
        new ServiceLoader().load(serviceProvider, loadedClasses );
    }

    @Test
    public void testMenuLoad() throws IOException, IllegalAccessException, InstantiationException
    {
        assertEquals(loadedClasses.get("menu"), Menu.class);
    }
}
