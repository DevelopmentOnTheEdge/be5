package com.developmentontheedge.be5.modules.core;

import com.developmentontheedge.be5.meta.ProjectProvider;
import com.developmentontheedge.be5.test.TestProjectProvider;
import com.developmentontheedge.be5.test.TestUtils;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.util.Modules;


public abstract class CoreBe5ProjectDBTest extends TestUtils
{
    static
    {
        addH2ProfileAndCreateDb();
    }

    private static final Injector injector = initInjector(
            Modules.override(
                    new CoreModule()
            ).with(new CoreDBTestModule())
    );

    private static class CoreDBTestModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bind(ProjectProvider.class).to(TestProjectProvider.class).in(Scopes.SINGLETON);
            install(new WebTestModule());
        }
    }

    @Override
    public Injector getInjector()
    {
        return injector;
    }
}
