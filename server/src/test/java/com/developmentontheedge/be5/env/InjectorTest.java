package com.developmentontheedge.be5.env;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.components.Document;
import com.developmentontheedge.be5.components.Menu;
import com.developmentontheedge.be5.components.StaticPageComponent;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;

public class InjectorTest
{
//    private static Injector injector = null;
//    private static ComponentProvider loadedClasses = null;
//    private static Injector injector = Be5.createInjector();
//
//    @Before
//    public void newContainers(){
//        injector = new Be5Injector();
//        loadedClasses = new MainComponentProvider();
//    }
//
//    @Test
//    public void testMenuLoad()
//    {
//        injector.loadModules(getReader(CONTEXT_FILE), injector, loadedClasses );
//        assertEquals(loadedClasses.get("menu").getClass(), Menu.class);
//    }
//
//    @Test
//    public void testLoad()
//    {
//        injector.loadModules(getReader(CONTEXT_FILE), injector, loadedClasses);
//        //moduleLoader.loadModules(getReader("src/test/resources/" + CONTEXT_FILE), injector, loadedClasses);
//        ConfigurationProvider.INSTANCE.loadConfiguration();
//        ConfigurationProvider.INSTANCE.loadModuleConfiguration(getReader(CONTEXT_FILE));
//
//        assertEquals(Document.class, loadedClasses.get("document").getClass());
//        assertEquals(StaticPageComponent.class, loadedClasses.get("static").getClass());
//    }
//
//    @Test(expected = Be5Exception.class)
//    public void testLoadTryRedefine()
//    {
//        injector.loadModules(getReader(CONTEXT_FILE), injector, loadedClasses);
//        injector.loadModules(getReader("src/test/resources/tryRedefineApp/" + CONTEXT_FILE), injector, loadedClasses);
//    }
//
//    private BufferedReader getReader(String file){
//        try
//        {
//            return new BufferedReader(new InputStreamReader(new FileInputStream(file)));
//        }
//        catch (FileNotFoundException e)
//        {
//            throw new RuntimeException(e);
//        }
//    }

}
