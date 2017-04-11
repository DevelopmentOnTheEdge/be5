package com.developmentontheedge.be5.env;

import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.impl.ComponentProvider;
import com.developmentontheedge.be5.api.impl.MainServiceProvider;
import com.developmentontheedge.be5.components.Menu;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;

public class ServiceLoaderTest
{
    private static ServiceProvider serviceProvider = null;
    private static ComponentProvider loadedClasses = null;
    private static final ServiceLoader serviceLoader = new ServiceLoader();

    @Before
    public void newContainers(){
        serviceProvider = new MainServiceProvider();
        loadedClasses = new ComponentProvider();
    }

    @Test
    public void testMenuLoad()
    {
        serviceLoader.loadModule(getReader("context.yaml"), serviceProvider, loadedClasses );
        assertEquals(loadedClasses.get("menu"), Menu.class);
    }

    @Test
    public void testLoad()
    {
        serviceLoader.loadModule(getReader("context.yaml"), serviceProvider, loadedClasses);
        serviceLoader.loadModule(getReader("src/test/resources/app/context.yaml"), serviceProvider, loadedClasses);
    }

    @Test(expected = Be5Exception.class)
    public void testLoadTryRedefine()
    {
        serviceLoader.loadModule(getReader("context.yaml"), serviceProvider, loadedClasses);
        serviceLoader.loadModule(getReader("src/test/resources/tryRedefineApp/context.yaml"), serviceProvider, loadedClasses);
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
