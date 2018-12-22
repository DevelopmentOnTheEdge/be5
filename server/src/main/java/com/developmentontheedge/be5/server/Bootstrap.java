package com.developmentontheedge.be5.server;

import com.developmentontheedge.be5.base.Service;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.database.DataSourceService;
import com.developmentontheedge.be5.database.DbService;
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
        try
        {
            initServices();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Be5 cannot init services. Please check log for further errors.", e);
        }
    }

    private void initServices() throws Exception
    {
        Service dataSourceService = injector.getInstance(DataSourceService.class);
        dataSourceService.start();
        //addShutdownHook(dataSourceService);

        injector.getInstance(DbService.class).start();
        injector.getInstance(UserAwareMeta.class).start();
        injector.getInstance(ProjectProvider.class).start();
        injector.getInstance(Meta.class).start();
    }

    public synchronized void shutdown()
    {
        injector.getInstance(DaemonStarter.class).shutdown();
    }

}
