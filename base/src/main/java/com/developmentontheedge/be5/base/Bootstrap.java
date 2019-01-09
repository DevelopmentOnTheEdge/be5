package com.developmentontheedge.be5.base;

import com.developmentontheedge.be5.base.lifecycle.LifecycleService;
import com.google.inject.Injector;

public class Bootstrap
{
    private final Injector injector;

    public Bootstrap(Injector injector)
    {
        this.injector = injector;
    }

    public synchronized void boot()
    {
        try
        {
            initServices();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Be5 cannot init services. Please check log for further errors.", e);
        }
    }

    private void initServices()
    {
        injector.getInstance(LifecycleService.class).start();
    }

    public synchronized void shutdown()
    {
        injector.getInstance(LifecycleService.class).stop();
    }
}
