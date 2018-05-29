package com.developmentontheedge.be5.server.test;

import com.developmentontheedge.be5.server.ServerModule;
import com.developmentontheedge.be5.server.servlet.TemplateModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;


public abstract class ServerBe5ProjectTest extends TestUtils
{
    private static final Injector injector = initInjector(
            Modules.override(new ServerModule(), new TemplateModule()).with(new SqlMockModule()),
            new CoreModuleForTest()
    );

    @Override
    public Injector getInjector()
    {
        return injector;
    }
}
