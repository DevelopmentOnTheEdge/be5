package com.developmentontheedge.be5.modules.core.components

import com.developmentontheedge.be5.api.Component
import com.developmentontheedge.be5.api.Request
import com.developmentontheedge.be5.api.Response
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.env.Injector
import com.developmentontheedge.be5.api.services.LoginService
import com.developmentontheedge.be5.test.AbstractProjectTest
import org.junit.Before
import org.junit.Test

import static org.mockito.Matchers.any
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

class LoginTest extends AbstractProjectTest
{
    @Inject Injector injector
    private static Component component
    
    @Before
    void init(){
        component = injector.getComponent("login")
    }

    @Test
    void login() throws Exception {
        String testUser = "testUser"
        String testPass = "testPass"
        Response response = mock(Response.class)
        Request request = getMockRequest("")
        when(request.get("username")).thenReturn(testUser)
        when(request.get("password")).thenReturn(testPass)

        Injector sp = mock(Injector.class)

        LoginService loginService = mock(LoginService.class)

        when(loginService.login(any(),any(),any())).thenReturn(true)

        when(sp.getLoginService()).thenReturn(loginService)
        component.generate(request, response, sp)

        verify(loginService).login(eq(request), eq(testUser), eq(testPass))

        verify(response).sendSuccess()
    }

    @Test
    void loginAccessDenied() throws Exception {
        String testUser = "testUser"
        String testPass = "testPass"
        Response response = mock(Response.class)
        Request request = getMockRequest("")
        when(request.get("username")).thenReturn(testUser)
        when(request.get("password")).thenReturn(testPass)

        Injector sp = mock(Injector.class)

        LoginService loginService = mock(LoginService.class)

        when(sp.getLoginService()).thenReturn(loginService)
        component.generate(request, response, sp)

        verify(response).sendError(eq("Access denied"), eq("loginError"))
    }

    @Test
    void error() throws Exception {
        Response response = mock(Response.class)

        component.generate(getMockRequest(""), response, injector)

        verify(response).sendError(eq("Empty username or password"), eq("loginError"))
    }

}