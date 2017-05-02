package com.developmentontheedge.be5.modules.core.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.services.LoginService;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LogoutTest extends AbstractProjectTest
{
    private static Component component;

    @BeforeClass
    public static void init(){
        component = loadedClasses.get("logout");
    }

    @Test
    public void logout() throws Exception {
        Request mockRequest = getMockRequest("");
        Response response = mock(Response.class);
        ServiceProvider sp = mock(ServiceProvider.class);

        LoginService loginService = mock(LoginService.class);

        when(sp.getLoginService()).thenReturn(loginService);

        component.generate(mockRequest, response, sp);

        verify(loginService).logout(mockRequest);
        verify(response).sendSuccess();
    }

}