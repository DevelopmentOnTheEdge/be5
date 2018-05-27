package com.developmentontheedge.be5.databasemodel;

import com.developmentontheedge.be5.base.BaseModule;
import com.developmentontheedge.be5.test.BaseTestUtils;
import com.google.inject.Injector;
import com.google.inject.util.Modules;


public abstract class DatabaseModelProjectDbTest extends BaseTestUtils
{
    private static final Injector injector = initInjector(
            Modules.override(new BaseModule()).with(new TestProjectProviderModule())
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
