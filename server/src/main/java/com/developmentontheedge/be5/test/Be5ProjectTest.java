package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.services.Be5Caches;
import com.developmentontheedge.be5.api.services.ConnectionService;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.inject.Binder;
import com.developmentontheedge.be5.inject.Injector;
import com.developmentontheedge.be5.inject.impl.YamlBinder;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.test.mocks.Be5CachesForTest;
import com.developmentontheedge.be5.test.mocks.ConnectionServiceMock;
import com.developmentontheedge.be5.test.mocks.DatabaseServiceMock;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import com.developmentontheedge.be5.api.helpers.UserHelper;
import org.junit.Before;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public abstract class Be5ProjectTest extends TestUtils
{
    private static final Injector injector = initInjector(new SqlMockBinder());

    @Before
    public void setUpBe5ProjectTest()
    {
        injector.injectAnnotatedFields(this);
        initGuest();
    }

    protected void initUserWithRoles(String... roles)
    {
        injector.get(UserHelper.class).saveUser(TEST_USER, Arrays.asList(roles), Arrays.asList(roles),
                Locale.US, "", new TestSession());
    }

    protected void initGuest()
    {
        List<String> roles = Collections.singletonList(RoleType.ROLE_GUEST);
        injector.get(UserHelper.class).saveUser(RoleType.ROLE_GUEST, roles, roles,
                Locale.US, "", new TestSession());
    }

    public static class SqlMockBinder implements Binder
    {
        @Override
        public void configure(Map<String, Class<?>> loadedClasses, Map<Class<?>, Class<?>> bindings,
                              Map<Class<?>, Object> configurations, List<Class<?>> requestPreprocessors)
        {
            new YamlBinder().configure(loadedClasses, bindings, configurations, requestPreprocessors);
            bindings.put(SqlService.class, SqlServiceMock.class);
            bindings.put(DatabaseService.class, DatabaseServiceMock.class);
            bindings.put(ConnectionService.class, ConnectionServiceMock.class);
            bindings.put(Be5Caches.class, Be5CachesForTest.class);
        }

        @Override
        public String getInfo()
        {
            return "";
        }
    }

    public Component getComponent(String componentId)
    {
        return (Component)injector.getComponent(componentId);
    }

}
