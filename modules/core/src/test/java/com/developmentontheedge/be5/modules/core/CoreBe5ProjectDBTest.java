package com.developmentontheedge.be5.modules.core;

import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.be5.server.ServerModule;
import com.developmentontheedge.be5.modules.core.CoreModule;
import com.developmentontheedge.be5.server.test.TestUtils;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;


public abstract class CoreBe5ProjectDBTest extends TestUtils
{
    private static final Injector injector = initInjector(
            Modules.override(
                    new ServerModule(),
                    new CoreModule()
            ).with(new ServerDBTestModule())
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
            install(new TestProjectProviderModule());
            bind(QuerySession.class).to(QuerySessionForTest.class);
        }
    }

    public static class QuerySessionForTest implements QuerySession
    {
        @Override
        public Object get(String name)
        {
            return null;
        }
    }
}
