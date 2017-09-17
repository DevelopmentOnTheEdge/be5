package com.developmentontheedge.be5.env.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.api.services.impl.LogConfigurator;
import com.developmentontheedge.be5.api.services.impl.ProjectProviderImpl;
import com.developmentontheedge.be5.components.Document;
import com.developmentontheedge.be5.components.StaticPageComponent;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.developmentontheedge.be5.env.impl.YamlBinder.CONTEXT_FILE;
import static org.junit.Assert.*;

public class YamlBinderTest
{
    private YamlBinder yamlBinder;
    private Map<String, Class<?>> loadedClasses;
    private Map<Class<?>, Class<?>> bindings;
    private Map<Class<?>, Object> configurations;

    @Before
    public void newContainers()
    {
        yamlBinder = new YamlBinder();//(loadedClasses, bindings, configurations);
        loadedClasses = new HashMap<>();
        bindings = new HashMap<>();
        configurations = new HashMap<>();
    }

    @Test
    public void test()
    {
        yamlBinder.loadModules(getReader(CONTEXT_FILE), bindings, loadedClasses );

        assertEquals(Document.class, loadedClasses.get("document"));
        assertEquals(StaticPageComponent.class, loadedClasses.get("static"));

        assertEquals(ProjectProviderImpl.class, bindings.get(ProjectProvider.class));
    }

    @Test
    public void testLoadConfig() throws FileNotFoundException
    {
        File file = new File(getClass().getClassLoader().getResource(CONTEXT_FILE).getFile());

        yamlBinder.loadModuleConfiguration(new BufferedReader(new FileReader(file)), configurations);

        assertEquals(Collections.singletonMap("path", "/logging.properties"),
                configurations.get(LogConfigurator.class));
    }

    @Test(expected = Be5Exception.class)
    public void testLoadTryRedefine()
    {
        yamlBinder.loadModules(getReader(CONTEXT_FILE), bindings, loadedClasses);
        yamlBinder.loadModules(getReader("src/test/resources/errorRedefine/" + CONTEXT_FILE), bindings, loadedClasses);
        assertEquals(Document.class, loadedClasses.get("document"));
    }

    @Test
    public void isServerTrue()
    {
        assertTrue(yamlBinder.isServer(getReader(CONTEXT_FILE)));
    }

    @Test
    public void isServerLoad()
    {
        yamlBinder = new YamlBinder(YamlBinder.Mode.serverOnly);
        yamlBinder.configure(loadedClasses, bindings, configurations);
        assertEquals(Document.class, loadedClasses.get("document"));
    }

    @Test
    public void isServerNotFoundFalse()
    {
        assertFalse(yamlBinder.isServer(getReader("src/test/resources/errorRedefine/" + CONTEXT_FILE)));
    }

    @Test(expected = Be5Exception.class)
    public void testLoadTryRedefineServices()
    {
        yamlBinder.loadModules(getReader(CONTEXT_FILE), bindings, loadedClasses);
        yamlBinder.loadModules(getReader("src/test/resources/errorRedefine/contextService.yaml"), bindings, loadedClasses);
        assertEquals(Document.class, loadedClasses.get("document"));
    }

    private BufferedReader getReader(String file){
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