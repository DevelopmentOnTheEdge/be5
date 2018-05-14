package com.developmentontheedge.be5.modules.core.components;

import com.developmentontheedge.be5.ServerModule;
import com.developmentontheedge.be5.modules.core.CoreModule;
import com.developmentontheedge.be5.test.TestUtils;
import com.google.inject.Injector;
import com.google.inject.util.Modules;


public abstract class CoreBe5ProjectTest extends TestUtils
{
    private static final Injector injector = initInjector(
            Modules.override(new ServerModule()).with(new SqlMockModule()),
            new CoreModule()
    );

    @Override
    public Injector getInjector()
    {
        return injector;
    }
}
