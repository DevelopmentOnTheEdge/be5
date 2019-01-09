package com.developmentontheedge.be5.operation;

import com.developmentontheedge.be5.base.BaseModule;
import com.developmentontheedge.be5.base.services.CoreUtils;
import com.developmentontheedge.be5.database.DatabaseModule;
import com.developmentontheedge.be5.test.mocks.CoreUtilsForTest;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;


public abstract class OperationBe5ProjectDBTest extends OperationTestUtils
{
    private static final Injector injector = initInjector(
            Modules.override(new BaseModule()).with(new TestQueryModule()),
            new OperationModule()
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
            install(new DatabaseModule());
            bind(CoreUtils.class).to(CoreUtilsForTest.class);
        }
    }

}
