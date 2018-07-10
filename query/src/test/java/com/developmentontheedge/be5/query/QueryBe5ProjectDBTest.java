package com.developmentontheedge.be5.query;

import com.developmentontheedge.be5.base.BaseModule;
import com.developmentontheedge.be5.base.services.CoreUtils;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.test.BaseTestUtils;
import com.developmentontheedge.be5.test.mocks.CoreUtilsForTest;
import com.developmentontheedge.be5.testbase.StaticUserInfoProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.util.Modules;


public abstract class QueryBe5ProjectDBTest extends BaseTestUtils
{
    private static final Injector injector = initInjector(
            Modules.override(new BaseModule()).with(new TestQueryModule()),
            new QueryModule()
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

    private static class TestQueryModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            install(new BaseDbTestModule());
            bind(QuerySession.class).to(QuerySessionForTest.class);
            bind(CoreUtils.class).to(CoreUtilsForTest.class);
            bind(UserInfoProvider.class).to(StaticUserInfoProvider.class).in(Scopes.SINGLETON);
        }
    }

    private static class QuerySessionForTest implements QuerySession
    {
        @Override
        public Object get(String name)
        {
            return null;
        }
    }
}
