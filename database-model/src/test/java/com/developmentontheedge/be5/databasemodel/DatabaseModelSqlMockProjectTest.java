package com.developmentontheedge.be5.databasemodel;

import com.developmentontheedge.be5.base.BaseModule;
import com.developmentontheedge.be5.base.config.CoreUtils;
import com.developmentontheedge.be5.database.DatabaseModule;
import com.developmentontheedge.be5.test.BaseTest;
import com.developmentontheedge.be5.test.mocks.CoreUtilsForTest;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.util.Modules;

import javax.inject.Inject;


public abstract class DatabaseModelSqlMockProjectTest extends BaseTest
{
    @Inject
    protected DatabaseModel database;

    private static final Injector injector = initInjector(
            Modules.override(new BaseModule(), new DatabaseModule()).with(new DatabaseModelSqlMockProjectTestModule())
    );

    @Override
    public Injector getInjector()
    {
        return injector;
    }

    public static class DatabaseModelSqlMockProjectTestModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            install(new BaseDbMockTestModule());
            bind(CoreUtils.class).to(CoreUtilsForTest.class).in(Scopes.SINGLETON);
        }
    }
}
