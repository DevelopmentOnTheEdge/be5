package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.impl.RequestImpl;
import com.developmentontheedge.be5.api.services.Be5MainSettings;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.LoginService;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.components.Form;
import com.developmentontheedge.be5.env.Binder;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.env.impl.YamlBinder;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.test.mocks.Be5MainSettingsForTest;
import com.developmentontheedge.be5.test.mocks.DatabaseServiceMock;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import org.junit.Before;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public abstract class Be5ProjectTest extends TestUtils
{
    private static final Injector injector = initInjector(new SqlMockBinder());

    @Before
    public void injectAnnotatedFields() {
        injector.injectAnnotatedFields(this);
    }

    protected void initUserWithRoles(String... roles)
    {
        LoginService loginService = injector.get(LoginService.class);
        loginService.saveUser("testUser", Arrays.asList(roles), Locale.US, "");
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
            bindings.put(Be5MainSettings.class, Be5MainSettingsForTest.class);
        }

        @Override
        public String getInfo()
        {
            return "";
        }
    }

}
