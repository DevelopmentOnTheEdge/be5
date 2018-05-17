package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.ServerModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;


public abstract class ServerBe5ProjectDBTest extends TestUtils
{
    private static final Injector injector = initInjector(
            Modules.override(new ServerModule()).with(new TestProjectProviderModule()),
            new CoreModuleForTest()
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
