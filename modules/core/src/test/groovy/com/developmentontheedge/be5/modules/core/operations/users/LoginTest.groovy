package com.developmentontheedge.be5.modules.core.operations.users

import com.developmentontheedge.be5.api.Request
import com.developmentontheedge.be5.api.Response
import com.developmentontheedge.be5.api.Session
import com.developmentontheedge.be5.api.helpers.UserInfoHolder
import com.developmentontheedge.be5.api.sql.ResultSetParser
import com.developmentontheedge.be5.components.FrontendConstants
import com.developmentontheedge.be5.env.Injector
import com.developmentontheedge.be5.api.services.LoginService
import com.developmentontheedge.be5.metadata.RoleType
import com.developmentontheedge.be5.operation.OperationStatus
import com.developmentontheedge.be5.test.Be5ProjectTest
import com.developmentontheedge.be5.test.mocks.SqlServiceMock
import com.developmentontheedge.beans.json.JsonFactory
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.Matchers

import static org.junit.Assert.assertEquals
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when


class LoginTest extends Be5ProjectTest
{
    @Before
    void init(){
        initUserWithRoles(RoleType.ROLE_GUEST)
    }

    @Test
    void generate()
    {
        def first = generateOperation(createOperation("users", "", "Login", "")).getFirst()

        assertEquals("{'values':{'user_name':'','user_pass':''},'meta':{'/user_name':{'displayName':'Логин','columnSize':'100'},'/user_pass':{'displayName':'Пароль','passwordField':true,'columnSize':'50'}},'order':['/user_name','/user_pass']}",
                oneQuotes(JsonFactory.bean(first)))
    }

    @Test
    void execute()
    {
        def request = mock(Request.class)
        UserInfoHolder.setRequest(request)

        def session = mock(Session.class)
        when(request.getSession()).thenReturn(session)

        String testPass = "testPass"

        when(SqlServiceMock.mock.getScalar(eq("SELECT COUNT(user_name) FROM users WHERE user_name = ? AND user_pass = ?"),
                eq(TEST_USER), eq(testPass))).thenReturn(1L)

        when(SqlServiceMock.mock.selectList(eq("SELECT role_name FROM user_roles WHERE user_name = ?"),
                Matchers.<ResultSetParser<String>>any(), eq(TEST_USER)))
                .thenReturn(Arrays.asList("Test1", "Test2"))

        def second = executeOperation(createOperation("users", "", "Login", ""),
                [user_name: TEST_USER, user_pass: testPass]).getSecond()

        assertEquals OperationStatus.FINISHED, second.getStatus()
        assertEquals FrontendConstants.REFRESH_ALL, second.getMessage()

        assertEquals TEST_USER, UserInfoHolder.getUserInfo().userName
        assertEquals Arrays.asList("Test1", "Test2"), UserInfoHolder.getUserInfo().availableRoles
        assertEquals session, UserInfoHolder.getUserInfo().session
    }

    @Test
    @Ignore
    void loginAccessDenied()
    {
        String testPass = "testPass"
        Response response = mock(Response.class)
        Request request = getMockRequest("")
        when(request.get("username")).thenReturn(TEST_USER)
        when(request.get("password")).thenReturn(testPass)

        Injector sp = mock(Injector.class)

        LoginService loginService = mock(LoginService.class)

        when(sp.getLoginService()).thenReturn(loginService)
        component.generate(request, response, sp)

        verify(response).sendError(eq("Access denied"), eq("loginError"))
    }

    @Test
    @Ignore
    void error()
    {
        Response response = mock(Response.class)

        component.generate(getMockRequest(""), response, injector)

        verify(response).sendError(eq("Empty username or password"), eq("loginError"))
    }

}