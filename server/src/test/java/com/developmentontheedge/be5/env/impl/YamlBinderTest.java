package com.developmentontheedge.be5.env.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.api.services.impl.LogConfigurator;
import com.developmentontheedge.be5.api.services.impl.ProjectProviderImpl;
import com.developmentontheedge.be5.components.Form;
import com.developmentontheedge.be5.components.StaticPageComponent;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
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

    @Test
    public void test()
    {
        yamlBinder.loadModules(getReader(CONTEXT_FILE), bindings, loadedClasses, configurations, requestPreprocessors);

        assertEquals(Form.class, loadedClasses.get("form"));
        assertEquals(StaticPageComponent.class, loadedClasses.get("static"));

        assertEquals(ProjectProviderImpl.class, bindings.get(ProjectProvider.class));
    }

    @Test
    public void testLoadConfig() throws FileNotFoundException
    {
        yamlBinder.loadModules(getReader(getClass().getClassLoader().getResource(CONTEXT_FILE).getFile()),
                bindings, loadedClasses, configurations, requestPreprocessors);

        assertEquals(Collections.singletonMap("path", "/logging.properties"),
                configurations.get(LogConfigurator.class));

        assertEquals(1, requestPreprocessors.size());
    }

    @Test(expected = RuntimeException.class)
    public void testLoadTryRedefine()
    {
        yamlBinder.loadModules(getReader(CONTEXT_FILE), bindings, loadedClasses, configurations, requestPreprocessors);
        yamlBinder.loadModules(getReader("src/test/resources/errorRedefine/" + CONTEXT_FILE), bindings, loadedClasses, configurations, requestPreprocessors);
        assertEquals(Form.class, loadedClasses.get("form"));
    }

    @Test
    public void isServerTrue()
    {
        assertTrue(yamlBinder.isServer(getReader(CONTEXT_FILE)));
    }

    @Test
    public void isServerNotFoundFalse()
    {
        assertFalse(yamlBinder.isServer(getReader("src/test/resources/errorRedefine/" + CONTEXT_FILE)));
    }

    @Test(expected = RuntimeException.class)
    public void testLoadTryRedefineServices()
    {
        yamlBinder.loadModules(getReader(CONTEXT_FILE), bindings, loadedClasses, configurations, requestPreprocessors);
        yamlBinder.loadModules(getReader("src/test/resources/errorRedefine/contextService.yaml"), bindings, loadedClasses, configurations, requestPreprocessors);
        assertEquals(Form.class, loadedClasses.get("form"));
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