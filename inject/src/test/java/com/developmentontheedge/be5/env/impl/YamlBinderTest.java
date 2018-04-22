package com.developmentontheedge.be5.env.impl;

import com.developmentontheedge.be5.env.impl.testComponents.TestComponent;
import com.developmentontheedge.be5.env.services.TestService;
import com.developmentontheedge.be5.env.services.impl.TestServiceMock;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.developmentontheedge.be5.env.impl.YamlBinder.CONTEXT_FILE;
import static org.junit.Assert.*;


public class YamlBinderTest
{
    private YamlBinder yamlBinder;
    private Map<String, Class<?>> loadedClasses;
    private Map<Class<?>, Class<?>> bindings;
    private List<Class<?>> requestPreprocessors;
    private Map<Class<?>, Object> configurations;


    @Before
    public void newContainers()
    {
        yamlBinder = new YamlBinder();//(loadedClasses, bindings, configurations);
        loadedClasses = new HashMap<>();
        bindings = new HashMap<>();
        requestPreprocessors = new ArrayList<>();
        configurations = new HashMap<>();
    }

    @Test(expected = RuntimeException.class)
    public void testLoadTryRedefine()
    {
        yamlBinder.loadModules(getReader(getClass().getClassLoader().getResource(CONTEXT_FILE).getFile()),
                bindings, loadedClasses, configurations, requestPreprocessors);
        yamlBinder.loadModules(getReader("src/test/resources/errorRedefine/" + CONTEXT_FILE),
                bindings, loadedClasses, configurations, requestPreprocessors);
    }

    @Test
    public void isServerTrue()
    {
        assertFalse(yamlBinder.isServer(getReader(getClass().getClassLoader().getResource(CONTEXT_FILE).getFile())));
    }

    @Test
    public void isServerNotFoundFalse()
    {
        assertFalse(yamlBinder.isServer(getReader("src/test/resources/errorRedefine/" + CONTEXT_FILE)));
    }

    @Test(expected = RuntimeException.class)
    public void testLoadTryRedefineServices()
    {
        yamlBinder.loadModules(getReader(getClass().getClassLoader().getResource(CONTEXT_FILE).getFile()),
                bindings, loadedClasses, configurations, requestPreprocessors);
        yamlBinder.loadModules(getReader("src/test/resources/errorRedefine/contextService.yaml"),
                bindings, loadedClasses, configurations, requestPreprocessors);
    }

    private BufferedReader getReader(String file)
    {
        try
        {
            return new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        }
        catch (FileNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

}