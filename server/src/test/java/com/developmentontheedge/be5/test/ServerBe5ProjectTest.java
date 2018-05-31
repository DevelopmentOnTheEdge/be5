package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.be5.server.ServerModule;
import com.developmentontheedge.be5.server.servlet.TemplateModule;
import com.developmentontheedge.be5.test.mocks.ServerTestQuerySession;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;


public abstract class ServerBe5ProjectTest extends ServerTestUtils
{
    private static final Injector injector = initInjector(
            Modules.override(new ServerModule(), new TemplateModule()).with(new ServerTestModule()),
            new CoreModuleForTest()
    );

    @Override
    public Injector getInjector()
    {
        return injector;
    }

    private static class ServerTestModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            install(new BaseDbMockTestModule());
            bind(QuerySession.class).to(ServerTestQuerySession.class);
        }
    }
}
