package com.developmentontheedge.be5.base;

import ch.qos.logback.classic.Level;
import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.CoreUtils;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.base.services.impl.LogConfigurator;
import com.developmentontheedge.be5.base.test.BaseCoreUtilsForTest;
import com.developmentontheedge.be5.metadata.RoleType;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import com.google.inject.util.Modules;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.Arrays;


public abstract class BaseTest
{
    static
    {
        LogConfigurator.configure();
        LogConfigurator.setLevel(Level.INFO);
    }

    private static final Injector injector = initInjector(
            Modules.override(new BaseModule()).with(new BaseTestModule())
    );

    protected static Injector initInjector(Module... modules)
    {
        Injector injector = Guice.createInjector(Stage.PRODUCTION, modules);
        new Bootstrap(injector).boot();
        return injector;
    }

    @Before
    public void setUpBaseTestUtils()
    {
        if (getInjector() != null)
        {
            getInjector().injectMembers(this);
        }
        setStaticUserInfo(RoleType.ROLE_GUEST);
    }

    protected void setStaticUserInfo(String... roles)
    {
        StaticUserInfoProvider.userInfo = new UserInfo("base_test_user", Arrays.asList(roles), Arrays.asList(roles));
        StaticUserInfoProvider.userInfo.setRemoteAddr("192.168.0.1");
    }

    public Injector getInjector()
    {
        return injector;
    }

    public static class BaseTestModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bind(UserInfoProvider.class).to(StaticUserInfoProvider.class).in(Scopes.SINGLETON);
            bind(CoreUtils.class).to(BaseCoreUtilsForTest.class).in(Scopes.SINGLETON);
        }
    }
}
