package com.developmentontheedge.be5.base;

import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.base.util.StaticUserInfoProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import com.google.inject.util.Modules;


public abstract class BaseTest
{
    private static final Injector injector = Guice.createInjector(Stage.DEVELOPMENT,
            Modules.override(new BaseModule()).with(new BaseTestModule())
    );

    public Injector getInjector()
    {
        return injector;
    }

    public static class BaseTestModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            install(new BaseModule());
            bind(UserInfoProvider.class).to(StaticUserInfoProvider.class).in(Scopes.SINGLETON);
        }
    }
}
