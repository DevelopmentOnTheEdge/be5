package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.ServerModule;
import com.developmentontheedge.be5.servlet.TemplateModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;


public abstract class ServerBe5ProjectTest extends TestUtils
{
    private static final Injector injector = initInjector(
            Modules.override(new ServerModule()).with(new SqlMockModule()),
            new TemplateModule(),
            new CoreModuleForTest()
    );

    @Override
    public Injector getInjector()
    {
        return injector;
    }
}
