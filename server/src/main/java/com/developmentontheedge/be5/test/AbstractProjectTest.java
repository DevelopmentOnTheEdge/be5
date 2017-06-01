package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.impl.RequestImpl;
import com.developmentontheedge.be5.api.services.impl.LoginServiceImpl;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.model.Project;
import org.mockito.Mockito;

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
    protected static Injector injector = Injector.createInjector();
    protected static final ServiceProvider sp = injector.getServiceProvider();

    private static final LoginServiceImpl loginService;

    static {
        Project project = injector.getServiceProvider().getProject();

        if(project.getProject().getLanguages().length == 0){
            project.getApplication().getLocalizations().addLocalization( "en", "test", Collections.singletonList("myTopic"), "foo", "bar" );
        }

        loginService = new LoginServiceImpl(null, sp.getProjectProvider());
        new LoginServiceImpl(null, sp.getProjectProvider()).initGuest(null);
    }

    protected Request getMockRequest(String requestUri){
        Request request = mock(Request.class);
        when(request.getRequestUri()).thenReturn(requestUri);
        return request;
    }

    protected Request getSpyMockRequest(String requestUri){
        return getSpyMockRequest(requestUri, new HashMap<>());
    }

    protected Request getSpyMockRequest(String requestUri, Map<String, String> parameters){
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getSession()).thenReturn(mock(HttpSession.class));

        Request request = Mockito.spy(new RequestImpl(httpServletRequest, null, parameters));
        when(request.getRequestUri()).thenReturn(requestUri);
        return request;
    }

    protected static void initUserWithRoles(String... roles)
    {
        loginService.saveUser("testUser", Arrays.asList(roles), Locale.US);
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
