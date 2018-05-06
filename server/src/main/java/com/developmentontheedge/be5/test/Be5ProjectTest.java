package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.ServerModule;
import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.services.Be5MainSettings;
import com.developmentontheedge.be5.api.services.CategoriesService;
import com.developmentontheedge.be5.api.services.ConnectionService;
import com.developmentontheedge.be5.api.services.CoreUtils;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.test.mocks.Be5MainSettingsForTest;
import com.developmentontheedge.be5.test.mocks.CategoriesServiceForTest;
import com.developmentontheedge.be5.test.mocks.ConnectionServiceMock;
import com.developmentontheedge.be5.test.mocks.CoreUtilsForTest;
import com.developmentontheedge.be5.test.mocks.DatabaseServiceMock;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import com.developmentontheedge.be5.api.helpers.UserHelper;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.util.Modules;
import org.junit.Before;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public abstract class Be5ProjectTest extends TestUtils
{
    private static final Injector injector = initInjector(
            Modules.override(new ServerModule()).with(new SqlMockBinder()));

    @Before
    public void setUpBe5ProjectTest()
    {
        injector.injectMembers(this);
        initGuest();
    }

    protected void initUserWithRoles(String... roles)
    {
        injector.getInstance(UserHelper.class).saveUser(TEST_USER, Arrays.asList(roles), Arrays.asList(roles),
                Locale.US, "", new TestSession());
    }

    protected void initGuest()
    {
        List<String> roles = Collections.singletonList(RoleType.ROLE_GUEST);
        injector.getInstance(UserHelper.class).saveUser(RoleType.ROLE_GUEST, roles, roles,
                Locale.US, "", new TestSession());
    }

    public static class SqlMockBinder extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bind(SqlService.class).to(SqlServiceMock.class).in(Scopes.SINGLETON);
            bind(DatabaseService.class).to(DatabaseServiceMock.class).in(Scopes.SINGLETON);
            bind(ConnectionService.class).to(ConnectionServiceMock.class).in(Scopes.SINGLETON);
            bind(Be5MainSettings.class).to(Be5MainSettingsForTest.class).in(Scopes.SINGLETON);

            bind(CoreUtils.class).to(CoreUtilsForTest.class).in(Scopes.SINGLETON);
            bind(CategoriesService.class).to(CategoriesServiceForTest.class).in(Scopes.SINGLETON);
        }
    }

}
