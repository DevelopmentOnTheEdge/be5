package com.developmentontheedge.be5;

import com.developmentontheedge.be5.api.ComponentProvider;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.impl.MainComponentProvider;
import com.developmentontheedge.be5.api.impl.MainServiceProvider;
import com.developmentontheedge.be5.env.ServerModuleLoader;
import org.junit.BeforeClass;

import java.io.IOException;

public abstract class AbstractProjectTest
{
    private static final ServiceProvider serviceProvider = new MainServiceProvider();
    private static final ServerModuleLoader moduleLoader = new ServerModuleLoader();
    private static final ComponentProvider loadedClasses = new MainComponentProvider();

    static {
        try
        {
            moduleLoader.load(serviceProvider, loadedClasses);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static ServiceProvider getServiceProvider(){
        return serviceProvider;
    }

}
