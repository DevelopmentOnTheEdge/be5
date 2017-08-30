package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.services.Be5MainSettings;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.LoginService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.env.Binder;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.impl.RequestImpl;
import com.developmentontheedge.be5.api.services.impl.LoginServiceImpl;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.developmentontheedge.be5.env.Be5;
import com.developmentontheedge.be5.env.impl.YamlBinder;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.be5.test.mocks.Be5MainSettingsForTest;
import com.developmentontheedge.be5.test.mocks.DatabaseServiceMock;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import com.google.common.collect.ImmutableMap;
import org.mockito.Mockito;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractProjectTest
{
    protected static final Injector injector = Be5.createInjector(new SqlMockBinder());
    protected static final Jsonb jsonb = JsonbBuilder.create();

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

    static final String profileForIntegrationTests = "profileForIntegrationTests";

    static {
        Project project = injector.getProject();
        initProfile(project);

        injector.get(LoginService.class).initGuest(null);
    }

    static void initProfile(Project project){
        if(project.getConnectionProfile() == null || !profileForIntegrationTests.equals(project.getConnectionProfile().getName()))
        {
            BeConnectionProfile profile = new BeConnectionProfile(profileForIntegrationTests, project.getConnectionProfiles().getLocalProfiles());
            profile.setConnectionUrl("jdbc:h2:~/"+ profileForIntegrationTests);
            profile.setUsername("sa");
            profile.setPassword("");
            profile.setDriverDefinition(Rdbms.H2.getDriverDefinition());
            DataElementUtils.save(profile);
            project.setConnectionProfileName(profileForIntegrationTests);
        }
    }

    protected Request getMockRequest(String requestUri){
        Request request = mock(Request.class);
        when(request.getRequestUri()).thenReturn(requestUri);
        return request;
    }

    protected Request getSpyMockRequest(String requestUri){
        return getSpyMockRequest(requestUri, new HashMap<>(), new HashMap<>());
    }

    protected Request getSpyMockRequest(String requestUri, Map<String, String> parameters, Map<String, Object> sessionValues){
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getSession()).thenReturn(mock(HttpSession.class));

        Request request = Mockito.spy(new RequestImpl(httpServletRequest, null, parameters));
        when(request.getRequestUri()).thenReturn(requestUri);

        for (Map.Entry<String, Object> entry: sessionValues.entrySet())
        {
            when(request.getAttribute(entry.getKey())).thenReturn(entry.getValue());
        }

        return request;
    }

    protected Request getSpyMockRecForOp(String entity, String query, String operation, String selectedRows, String values, Map<String, Object> sessionValues)
    {
        return getSpyMockRequest("", ImmutableMap.of(
                RestApiConstants.ENTITY, entity,
                RestApiConstants.QUERY, query,
                RestApiConstants.OPERATION, operation,
                RestApiConstants.SELECTED_ROWS, selectedRows,
                RestApiConstants.VALUES, values),
                sessionValues
        );
    }

    protected Request getSpyMockRecForOp(String entity, String query, String operation, String selectedRows, String values){
        return getSpyMockRecForOp(entity, query, operation, selectedRows, values, new HashMap<>());
    }

    protected static void initUserWithRoles(String... roles)
    {
        injector.get(LoginService.class).saveUser("testUser", Arrays.asList(roles), Locale.US, "");
    }

    protected static String oneQuotes(String s)
    {
        return s.replace("\"", "'");
    }

    protected static String doubleQuotes(String s)
    {
        return s.replace("'", "\"");
    }
}
