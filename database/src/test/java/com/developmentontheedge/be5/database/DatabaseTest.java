package com.developmentontheedge.be5.database;

import com.developmentontheedge.be5.base.Bootstrap;
import com.developmentontheedge.be5.base.lifecycle.LifecycleSupport;
import com.developmentontheedge.be5.base.services.Be5Caches;
import com.developmentontheedge.be5.base.services.CoreUtils;
import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.base.services.impl.Be5CachesImpl;
import com.developmentontheedge.be5.database.impl.test.TestTransaction2Service;
import com.developmentontheedge.be5.database.impl.test.TestTransaction2ServiceImpl;
import com.developmentontheedge.be5.database.impl.test.TestTransactionService;
import com.developmentontheedge.be5.database.test.BaseCoreUtilsForTest;
import com.developmentontheedge.be5.database.test.EmptyTestProjectProvider;
import com.developmentontheedge.be5.database.test.StaticUserInfoProvider;
import com.developmentontheedge.be5.database.test.TestH2DataSourceService;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import com.google.inject.util.Modules;
import org.junit.Before;

import javax.inject.Inject;


public abstract class DatabaseTest
{
    protected static final Injector injector = initInjector(
            Modules.override(new DatabaseModule()).with(new DatabaseModuleTestModule())
    );

    protected static Injector initInjector(Module... modules)
    {
        Injector injector = Guice.createInjector(Stage.PRODUCTION, modules);
        new Bootstrap(injector).boot();
        return injector;
    }

    @Inject
    protected DbService db;

    @Before
    public void setUpBaseTestUtils()
    {
        injector.injectMembers(this);
    }

    public static class DatabaseModuleTestModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            install(LifecycleSupport.getModule());
            bind(DataSourceService.class).to(TestH2DataSourceService.class).in(Scopes.SINGLETON);
            bind(TestTransactionService.class).in(Scopes.SINGLETON);
            bind(TestTransaction2Service.class).to(TestTransaction2ServiceImpl.class).in(Scopes.SINGLETON);
            bind(UserInfoProvider.class).to(StaticUserInfoProvider.class).in(Scopes.SINGLETON);
            bind(CoreUtils.class).to(BaseCoreUtilsForTest.class).in(Scopes.SINGLETON);
            bind(Be5Caches.class).to(Be5CachesImpl.class).in(Scopes.SINGLETON);
            bind(ProjectProvider.class).to(EmptyTestProjectProvider.class).in(Scopes.SINGLETON);
        }
    }
}
