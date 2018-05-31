package com.developmentontheedge.be5.modules.core;

import com.developmentontheedge.be5.base.UserInfoProvider;
import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.server.ServerModule;
import com.developmentontheedge.be5.test.TestProjectProvider;
import com.developmentontheedge.be5.test.TestUtils;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Scopes;
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

    private static class ServerDBTestModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bind(ProjectProvider.class).to(TestProjectProvider.class).in(Scopes.SINGLETON);
            bind(UserInfoProvider.class).to(UserInfoProviderForTest.class).in(Scopes.SINGLETON);
            install(new WebTestModule());
        }
    }

    @Override
    public Injector getInjector()
    {
        return injector;
    }
}
