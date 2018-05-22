package com.developmentontheedge.be5.modules.core.operations.users

import com.developmentontheedge.be5.api.Request
import com.developmentontheedge.be5.api.Session
import com.developmentontheedge.be5.servlet.UserInfoHolder
import com.developmentontheedge.be5.api.sql.ResultSetParser
import com.developmentontheedge.be5.metadata.DatabaseConstants
import com.developmentontheedge.be5.metadata.RoleType
import com.developmentontheedge.be5.modules.core.controllers.CoreBe5ProjectTest
import com.developmentontheedge.be5.model.FrontendAction
import com.developmentontheedge.be5.modules.core.api.CoreFrontendActions
import com.developmentontheedge.be5.modules.core.model.UserInfoModel
import com.developmentontheedge.be5.operation.OperationStatus
import com.developmentontheedge.be5.test.mocks.DbServiceMock
import com.developmentontheedge.beans.json.JsonFactory
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.Matchers

import static org.junit.Assert.assertEquals
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when


class LoginTest extends CoreBe5ProjectTest
{
    @Before
    void init(){
        initUserWithRoles(RoleType.ROLE_GUEST)
    }

    @Test
    void generate()
    {
        def first = generateOperation(createOperation("users", "All records", "Login", "")).getFirst()

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

        when(DbServiceMock.mock.one(eq("SELECT COUNT(user_name) FROM users WHERE user_name = ? AND user_pass = ?"),
                eq(TEST_USER), eq(testPass))).thenReturn(1L)

        when(DbServiceMock.mock.list(eq("SELECT role_name FROM user_roles WHERE user_name = ?"),
                Matchers.<ResultSetParser<String>>any(), eq(TEST_USER)))
                .thenReturn(Arrays.asList("Test1", "Test2"))

        when(DbServiceMock.mock.one(eq("SELECT pref_value FROM user_prefs WHERE pref_name = ? AND user_name = ?"),
                eq(DatabaseConstants.CURRENT_ROLE_LIST), eq(TEST_USER)))
                .thenReturn("('Test1')")

        def second = executeOperation(createOperation("users", "All records", "Login", ""),
                [user_name: TEST_USER, user_pass: testPass]).getSecond()

        assertEquals OperationStatus.FINISHED, second.getStatus()
        assertEquals null, second.getMessage()

        assertEquals TEST_USER, UserInfoHolder.getUserInfo().userName
        assertEquals Arrays.asList("Test1", "Test2"), UserInfoHolder.getUserInfo().availableRoles
        assertEquals Arrays.asList("Test1"), UserInfoHolder.getUserInfo().currentRoles
        assertEquals session, UserInfoHolder.getSession()

        def actions = (FrontendAction[]) second.getDetails()

        assertEquals(CoreFrontendActions.UPDATE_USER_INFO, actions[0].getType())

        def userInfoModel = (UserInfoModel) actions[0].getValue()
        assertEquals TEST_USER, userInfoModel.getUserName()
        assertEquals(Arrays.asList("Test1", "Test2"), userInfoModel.getAvailableRoles())

        assertEquals(CoreFrontendActions.OPEN_DEFAULT_ROUTE, actions[1].getType())
    }

    @Test
    @Ignore
    void loginAccessDenied()
    {
//        String testPass = "testPass"
//        Response response = mock(Response.class)
//        Request request = getMockRequest("")
//        when(request.get("username")).thenReturn(TEST_USER)
//        when(request.get("password")).thenReturn(testPass)
//
//        Injector sp = mock(Injector.class)
//
//        LoginService loginService = mock(LoginService.class)
//
//        when(sp.getLoginService()).thenReturn(loginService)
//        component.generate(request, response, sp)
//
//        verify(response).sendError(eq("Access denied"), eq("loginError"))
    }

    @Test
    @Ignore
    void error()
    {
//        Response response = mock(Response.class)
//
//        component.generate(getMockRequest(""), response, injector)
//
//        verify(response).sendError(eq("Empty username or password"), eq("loginError"))
    }

}