package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.ComponentProvider;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.impl.MainComponentProvider;
import com.developmentontheedge.be5.api.impl.MainServiceProvider;
import com.developmentontheedge.be5.api.impl.RequestImpl;
import com.developmentontheedge.be5.api.services.impl.LoginServiceImpl;
import com.developmentontheedge.be5.env.ServerModuleLoader;
import com.developmentontheedge.be5.metadata.model.Project;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.mockito.Mockito.mock;

public abstract class AbstractProjectTest
{
    private static final ServerModuleLoader moduleLoader = new ServerModuleLoader();

    protected static final ServiceProvider sp = new MainServiceProvider();
    protected static final ComponentProvider loadedClasses = new MainComponentProvider();

    protected static final LoginServiceImpl loginService ;

    static {
        try
        {
            moduleLoader.load(sp, loadedClasses);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        Project project = sp.getProject().getProject();

        if(project.getProject().getLanguages().length == 0){
            project.getApplication().getLocalizations().addLocalization( "en", "test", Arrays.asList("myTopic"), "foo", "bar" );
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
