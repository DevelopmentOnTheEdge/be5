package com.developmentontheedge.be5.env;

import com.developmentontheedge.be5.api.ComponentProvider;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.impl.MainComponentProvider;
import com.developmentontheedge.be5.api.impl.MainServiceProvider;
import com.developmentontheedge.be5.components.Menu;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;

public class ServerModuleLoaderTest
{
    private static ServiceProvider serviceProvider = null;
    private static ComponentProvider loadedClasses = null;
    private static final ServerModuleLoader moduleLoader = new ServerModuleLoader();

    @Before
    public void newContainers(){
        serviceProvider = new MainServiceProvider();
        loadedClasses = new MainComponentProvider();
    }

    @Test
    public void testMenuLoad()
    {
        moduleLoader.loadModules(getReader("context.yaml"), serviceProvider, loadedClasses );
        assertEquals(loadedClasses.get("menu"), Menu.class);
    }

    @Test
    public void testLoad()
    {
        moduleLoader.loadModules(getReader("context.yaml"), serviceProvider, loadedClasses);
        moduleLoader.loadModules(getReader("src/test/resources/app/context.yaml"), serviceProvider, loadedClasses);
    }

    @Test(expected = Be5Exception.class)
    public void testLoadTryRedefine()
    {
        moduleLoader.loadModules(getReader("context.yaml"), serviceProvider, loadedClasses);
        moduleLoader.loadModules(getReader("src/test/resources/tryRedefineApp/context.yaml"), serviceProvider, loadedClasses);
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
