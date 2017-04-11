package com.developmentontheedge.be5.env;

import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.impl.MainServiceProvider;
import com.developmentontheedge.be5.components.Menu;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;

public class ServiceLoaderTest
{
    private static final ServiceLoader serviceLoader = new ServiceLoader();
    private static final ServiceProvider serviceProvider = new MainServiceProvider();
    private static final Map<String, Class<?>> loadedClasses = new ConcurrentHashMap<>();

    @Before
    public void load() throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream("context.yaml")));
        serviceLoader.loadModule(reader, serviceProvider, loadedClasses );
    }

    @Test
    public void testMenuLoad() throws IOException, IllegalAccessException, InstantiationException
    {
        assertEquals(loadedClasses.get("menu"), Menu.class);
    }

    @Test
    public void testVersion() throws IOException, IllegalAccessException, InstantiationException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream("src/test/resources/app/context.yaml")));

        serviceLoader.loadModule(reader, serviceProvider, loadedClasses);
    }
}
