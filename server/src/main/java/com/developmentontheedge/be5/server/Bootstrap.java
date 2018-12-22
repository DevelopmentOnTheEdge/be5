package com.developmentontheedge.be5.server;

import com.developmentontheedge.be5.server.services.DaemonStarter;
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

    }

    public synchronized void shutdown()
    {
        injector.getInstance(DaemonStarter.class).shutdown();
    }

}
