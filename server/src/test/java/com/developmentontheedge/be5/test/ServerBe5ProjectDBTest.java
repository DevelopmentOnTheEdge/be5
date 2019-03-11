package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.cache.Be5Caches;
import com.developmentontheedge.be5.meta.ProjectProvider;
import com.developmentontheedge.be5.server.ServerModule;
import com.developmentontheedge.be5.server.authentication.rememberme.PersistentTokenRepository;
import com.developmentontheedge.be5.server.authentication.RoleService;
import com.developmentontheedge.be5.server.authentication.InitUserService;
import com.developmentontheedge.be5.server.services.OperationLogging;
import com.developmentontheedge.be5.server.services.events.Be5EventTestLogger;
import com.developmentontheedge.be5.test.mocks.Be5CachesForTest;
import com.developmentontheedge.be5.test.mocks.InitUserServiceMock;
import com.developmentontheedge.be5.test.mocks.OperationLoggingMock;
import com.developmentontheedge.be5.test.mocks.RememberUserHelperMock;
import com.developmentontheedge.be5.test.mocks.RoleServiceMock;
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

    static
    {
        initDb();
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
            bind(Be5EventTestLogger.class).asEagerSingleton();
            bind(OperationLogging.class).to(OperationLoggingMock.class).in(Scopes.SINGLETON);
            bind(RoleService.class).to(RoleServiceMock.class).in(Scopes.SINGLETON);
            bind(InitUserService.class).to(InitUserServiceMock.class).in(Scopes.SINGLETON);
            bind(PersistentTokenRepository.class).to(RememberUserHelperMock.class).in(Scopes.SINGLETON);
        }
    }

}
