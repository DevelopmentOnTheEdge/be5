package com.developmentontheedge.be5.database.test;

import com.developmentontheedge.be5.database.DataSourceService;
import com.developmentontheedge.be5.database.DatabaseModule;
import com.developmentontheedge.be5.database.DbService;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import com.google.inject.util.Modules;
import org.junit.Before;

import java.util.logging.LogManager;


public abstract class DatabaseTest
{
    private static final Injector injector = Guice.createInjector(Stage.DEVELOPMENT,
            Modules.override(new DatabaseModule()).with(new DatabaseModuleTestModule())
    );

    @Inject protected DbService db;

    @Before
    public void setUpBaseTestUtils()
    {
        if(getInjector() != null)
        {
            getInjector().injectMembers(this);
        }
    }

    public Injector getInjector()
    {
        return injector;
    }

    public static class DatabaseModuleTestModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bind(DataSourceService.class).to(DataSourceServiceTestImpl.class).in(Scopes.SINGLETON);
        }
    }

    static {
        LogManager.getLogManager().reset();
    }

}
