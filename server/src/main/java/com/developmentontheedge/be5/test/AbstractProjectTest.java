package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.impl.RequestImpl;
import com.developmentontheedge.be5.api.services.impl.LoginServiceImpl;
import com.developmentontheedge.be5.env.ServerModules;
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

public abstract class AbstractProjectTest
{
    protected static final ServiceProvider sp = ServerModules.getServiceProvider();

    protected static final LoginServiceImpl loginService ;

    static {
        Project project = ServerModules.getServiceProvider().getProject();

        if(project.getProject().getLanguages().length == 0){
            project.getApplication().getLocalizations().addLocalization( "en", "test", Collections.singletonList("myTopic"), "foo", "bar" );
        }

        loginService = new LoginServiceImpl(null, sp.getProjectProvider());
        loginService.initGuest(null, sp);
    }

    protected Request getMockRequest(String requestUri){
        Request request = mock(Request.class);
        Mockito.when(request.getRequestUri()).thenReturn(requestUri);
        return request;
    }

    protected Request getSpyMockRequest(String requestUri){
        return getSpyMockRequest(requestUri, new HashMap<>());
    }

    protected Request getSpyMockRequest(String requestUri, Map<String, String> parameters){
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        Mockito.when(httpServletRequest.getSession()).thenReturn(mock(HttpSession.class));

        Request request = Mockito.spy(new RequestImpl(httpServletRequest, null, parameters));
        Mockito.when(request.getRequestUri()).thenReturn(requestUri);
        return request;
    }

    protected void initUserWithRoles(String... roles)
    {
        loginService.saveUser("testUser", Arrays.asList(roles), Locale.US);
    }

}
