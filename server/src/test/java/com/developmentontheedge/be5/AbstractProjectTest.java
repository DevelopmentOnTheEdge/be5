package com.developmentontheedge.be5;

import com.developmentontheedge.be5.api.ComponentProvider;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.impl.MainComponentProvider;
import com.developmentontheedge.be5.api.impl.MainServiceProvider;
import com.developmentontheedge.be5.env.ServerModuleLoader;

import java.io.IOException;

public abstract class AbstractProjectTest
{
    protected static final ServiceProvider sp = new MainServiceProvider();
    private static final ServerModuleLoader moduleLoader = new ServerModuleLoader();
    private static final ComponentProvider loadedClasses = new MainComponentProvider();

    static {
        try
        {
            moduleLoader.load(sp, loadedClasses);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
