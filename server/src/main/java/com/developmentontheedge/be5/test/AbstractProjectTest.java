package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.services.Be5MainSettings;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.LoginService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.env.Binder;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.env.impl.YamlBinder;
import com.developmentontheedge.be5.test.mocks.Be5MainSettingsForTest;
import com.developmentontheedge.be5.test.mocks.DatabaseServiceMock;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import org.junit.Before;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;


public abstract class AbstractProjectTest extends TestUtils
{
    protected static final Jsonb jsonb = JsonbBuilder.create();
    protected static Injector injector = null;

    @Before
    public void injectMembers() {
        injector.injectAnnotatedFields(this);
    }

    protected static void initUserWithRoles(String... roles)
    {
        LoginService loginService = injector.get(LoginService.class);
        loginService.saveUser("testUser", Arrays.asList(roles), Locale.US, "");
    }

    static
    {
        injector = initInjector(new SqlMockBinder());
    }

    public static class SqlMockBinder implements Binder
    {
        @Override
        public void configure(Map<String, Class<?>> loadedClasses, Map<Class<?>, Class<?>> bindings,
                              Map<Class<?>, Object> configurations)
        {
            new YamlBinder().configure(loadedClasses, bindings, configurations);
            bindings.put(SqlService.class, SqlServiceMock.class);
            bindings.put(DatabaseService.class, DatabaseServiceMock.class);
            bindings.put(Be5MainSettings.class, Be5MainSettingsForTest.class);
        }
    }


}
