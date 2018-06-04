package com.developmentontheedge.be5.base;

import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.metadata.RoleType;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import com.google.inject.util.Modules;
import org.junit.Before;

import java.util.Arrays;


public abstract class BaseTest
{
    private static final Injector injector = Guice.createInjector(Stage.DEVELOPMENT,
            Modules.override(new BaseModule()).with(new BaseTestModule())
    );

    @Before
    public void setUpBaseTestUtils()
    {
        if(getInjector() != null)
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
            install(new BaseModule());
            bind(UserInfoProvider.class).to(StaticUserInfoProvider.class).in(Scopes.SINGLETON);
        }
    }
}
