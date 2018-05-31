package com.developmentontheedge.be5.modules.core;

import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.be5.server.ServerModule;
import com.developmentontheedge.be5.test.TestUtils;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;


public abstract class CoreBe5ProjectTest extends TestUtils
{
    private static final Injector injector = initInjector(
            Modules.override(
                    new ServerModule(),
                    new CoreModule()
            ).with(new DbMockTestModule())
    );

    @Override
    public Injector getInjector()
    {
        return injector;
    }
}
