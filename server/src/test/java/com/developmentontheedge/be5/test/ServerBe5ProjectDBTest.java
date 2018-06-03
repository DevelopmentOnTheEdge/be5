package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.base.services.Be5Caches;
import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.server.ServerModule;
import com.developmentontheedge.be5.test.mocks.Be5CachesForTest;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Scopes;
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
            bind(ProjectProvider.class).to(TestProjectProvider.class).in(Scopes.SINGLETON);
            bind(Be5Caches.class).to(Be5CachesForTest.class).in(Scopes.SINGLETON);
            install(new ServerWebTestModule());
        }
    }

}
