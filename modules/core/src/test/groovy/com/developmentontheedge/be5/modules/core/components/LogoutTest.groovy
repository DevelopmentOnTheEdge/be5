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

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

class LogoutTest extends AbstractProjectTest
{
    @Inject Injector injector
    private static Component component

    @Before
    void init(){
        component = injector.getComponent("logout")
    }

    @Test
    void logout() throws Exception {
        Request mockRequest = getMockRequest("")
        Response response = mock(Response.class)
        Injector injector1 = mock(Injector.class)

        LoginService loginService = mock(LoginService.class)

        when(injector1.getLoginService()).thenReturn(loginService)

        component.generate(mockRequest, response, injector1)

        verify(loginService).logout(mockRequest)
        verify(response).sendSuccess()
    }

}