package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.be5.server.ServerModule;
import com.developmentontheedge.be5.test.mocks.ServerTestQuerySession;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;


public abstract class ServerBe5ProjectDBTest extends ServerTestUtils
{
    private static final Injector injector = initInjector(
            Modules.override(new ServerModule()).with(new ServerDBTestModule()),
            new CoreModuleForTest()
    );

    static {
        initDb(injector);
    }

    @Override
    public Injector getInjector()
    {
        return injector;
    }

    private static class ServerDBTestModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            install(new BaseDbTestModule());
            install(new ServerWebTestModule());
            bind(QuerySession.class).to(ServerTestQuerySession.class);
        }
    }

}
