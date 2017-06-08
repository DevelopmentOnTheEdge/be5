package com.developmentontheedge.be5.env;

import com.developmentontheedge.be5.modules.core.components.Login;
import com.developmentontheedge.be5.modules.core.components.Logout;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;

public class InjectorCoreTest extends AbstractProjectTest
{
//    private static Injector injector = null;
//    private static ComponentProvider loadedClasses = null;
//
//    @Before
//    public void newContainers(){
//        injector = new Be5Injector();
//        loadedClasses = new MainComponentProvider();
//    }
//
//    @Test
//    public void testLoad()
//    {
//        injector.loadModules(getReader("src/test/resources/" + CONTEXT_FILE), injector, loadedClasses);
//        ConfigurationProvider.INSTANCE.loadConfiguration();
//        ConfigurationProvider.INSTANCE.loadModuleConfiguration(getReader("src/test/resources/" + CONTEXT_FILE));
//
//        assertEquals(Login.class, loadedClasses.get("login").getClass());
//        assertEquals(Logout.class, loadedClasses.get("logout").getClass());
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
