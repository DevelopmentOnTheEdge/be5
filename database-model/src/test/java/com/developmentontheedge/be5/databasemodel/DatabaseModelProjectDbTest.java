package com.developmentontheedge.be5.databasemodel;

import com.developmentontheedge.be5.base.BaseModule;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.test.BaseTestUtils;
import com.developmentontheedge.be5.testbase.StaticUserInfoProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.util.Modules;

import javax.inject.Inject;


public abstract class DatabaseModelProjectDbTest extends BaseTestUtils
{
    @Inject protected DatabaseModel database;

    private static final Injector injector = initInjector(
            Modules.override(new BaseModule()).with(new DatabaseModelDbTestModule())
    );

    static {
        initDb(injector);
    }

    @Override
    public Injector getInjector()
    {
        return injector;
    }

    public static class DatabaseModelDbTestModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            install(new BaseDbTestModule());
            bind(UserInfoProvider.class).to(StaticUserInfoProvider.class).in(Scopes.SINGLETON);
        }
    }
}
