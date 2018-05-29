package com.developmentontheedge.be5.modules.core.controllers;

import com.developmentontheedge.be5.ServerModule;
import com.developmentontheedge.be5.modules.system.SystemModule;
import com.developmentontheedge.be5.test.TestUtils;
import com.google.inject.Injector;
import com.google.inject.util.Modules;


public abstract class SystemBe5ProjectDBTest extends TestUtils
{
    private static final Injector injector = initInjector(
            Modules.override(
                    new ServerModule(),
                    new SystemModule(),
                    new CoreModuleForTest()
            ).with(new TestProjectProviderModule())
    );

    static {
        initDb(injector);
    }

    @Override
    public Injector getInjector()
    {
        return injector;
    }
}
