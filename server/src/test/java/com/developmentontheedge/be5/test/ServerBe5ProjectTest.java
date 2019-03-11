package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.server.ServerModule;
import com.developmentontheedge.be5.server.authentication.rememberme.PersistentTokenRepository;
import com.developmentontheedge.be5.server.authentication.RoleService;
import com.developmentontheedge.be5.server.authentication.InitUserService;
import com.developmentontheedge.be5.server.services.OperationLogging;
import com.developmentontheedge.be5.server.services.events.Be5EventTestLogger;
import com.developmentontheedge.be5.server.servlet.TemplateModule;
import com.developmentontheedge.be5.test.mocks.InitUserServiceMock;
import com.developmentontheedge.be5.test.mocks.OperationLoggingMock;
import com.developmentontheedge.be5.test.mocks.RememberUserHelperMock;
import com.developmentontheedge.be5.test.mocks.RoleServiceMock;
import com.developmentontheedge.be5.web.WebModule;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.util.Modules;


public abstract class ServerBe5ProjectTest extends ServerTestUtils
{
    private static final Injector injector = initInjector(
            Modules.override(new ServerModule(), new WebModule(), new TemplateModule()).with(new ServerTestModule()),
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
            install(new ServerWebTestModule());
            bind(Be5EventTestLogger.class).asEagerSingleton();
            bind(OperationLogging.class).to(OperationLoggingMock.class).in(Scopes.SINGLETON);
            bind(RoleService.class).to(RoleServiceMock.class).in(Scopes.SINGLETON);
            bind(InitUserService.class).to(InitUserServiceMock.class).in(Scopes.SINGLETON);
            bind(PersistentTokenRepository.class).to(RememberUserHelperMock.class).in(Scopes.SINGLETON);
        }
    }
}
