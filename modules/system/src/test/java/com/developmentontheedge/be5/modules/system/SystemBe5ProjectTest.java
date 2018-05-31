package com.developmentontheedge.be5.modules.system;

import com.developmentontheedge.be5.server.ServerModule;
import com.developmentontheedge.be5.test.TestUtils;
import com.google.inject.Injector;
import com.google.inject.util.Modules;


public abstract class SystemBe5ProjectTest extends TestUtils
{
    private static final Injector injector = initInjector(
            Modules.override(
                    new ServerModule(),
                    new SystemModule(),
                    new CoreTestModule()
            ).with(new DbMockTestModule())
    );

    @Override
    public Injector getInjector()
    {
        return injector;
    }
}
