package com.developmentontheedge.be5.databasemodel;

import com.developmentontheedge.be5.base.BaseModule;
import com.developmentontheedge.be5.test.BaseTestUtils;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import javax.inject.Inject;


public abstract class DatabaseModelSqlMockProjectTest extends BaseTestUtils
{
    @Inject protected DatabaseModel database;

    private static final Injector injector = initInjector(
            Modules.override(new BaseModule()).with(new BaseDbMockTestModule())
    );

    @Override
    public Injector getInjector()
    {
        return injector;
    }
}
