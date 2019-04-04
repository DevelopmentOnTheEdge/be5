package com.developmentontheedge.be5.server.authentication;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.security.UserInfo;
import com.developmentontheedge.be5.security.UserInfoHolder;
import com.developmentontheedge.be5.server.SessionConstants;
import com.developmentontheedge.be5.server.authentication.rememberme.AbstractRememberMeService;
import com.developmentontheedge.be5.server.services.events.Be5EventTestLogger;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import com.developmentontheedge.be5.test.ServerTestResponse;
import com.developmentontheedge.be5.test.mocks.InitUserServiceMock;
import com.developmentontheedge.be5.test.mocks.RoleServiceMock;
import com.developmentontheedge.be5.test.mocks.ServerTestRequest;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.web.Session;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.developmentontheedge.be5.server.authentication.rememberme.AbstractRememberMeService.REMEMBER_ME_KEY;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class UserServiceTest extends ServerBe5ProjectTest
{
    @Inject
    private UserService userService;
    @Inject
    private Provider<Request> requestProvider;
    @Inject
    private Provider<Response> responseProvider;

    @Test
    public void simpleSaveUser()
    {
        when(RoleServiceMock.mock.getAvailableRoles("test")).thenReturn(Arrays.asList("1", "2"));
        when(RoleServiceMock.mock.getCurrentRoles("test")).thenReturn(singletonList("1"));
        UserInfo ui = userService.saveUser("test");

        assertEquals(singletonList("1"), ui.getCurrentRoles());
    }

    @Test
    public void saveUser()
    {
        List<String> roles = Arrays.asList("1", "2");
        UserInfo ui = userService.saveUser("test", roles, roles, Locale.US, "192.168.0.1");

        assertEquals(roles, ui.getCurrentRoles());

        assertEquals(ui, UserInfoHolder.getLoggedUser());
        Session session = requestProvider.get().getSession();
        assertEquals("192.168.0.1", session.get("remoteAddr"));
        assertEquals(ui, session.get(SessionConstants.USER_INFO));
        assertEquals("test", session.get(SessionConstants.CURRENT_USER));

        verify(InitUserServiceMock.mock).initUser("test");
        assertNull(((ServerTestResponse) responseProvider.get()).getCookie(REMEMBER_ME_KEY));
    }

    @Test
    public void saveUserAndRemember()
    {
        ((ServerTestRequest) requestProvider.get())
                .setParameter(AbstractRememberMeService.DEFAULT_PARAMETER, "true");
        List<String> roles = Arrays.asList("1", "2");
        userService.saveUser("test", roles, roles, Locale.US, "192.168.0.1");

        Cookie cookie = ((ServerTestResponse) responseProvider.get()).getCookie(REMEMBER_ME_KEY);
        assertNotNull(cookie);
        assertEquals(REMEMBER_ME_KEY, cookie.getName());
    }

    @Test
    public void testGuestLocale()
    {
        initGuest();
        assertEquals("ru", userInfoProvider.getLanguage());
    }

    @Test
    public void testSetCurrentRoles()
    {
        initUserWithRoles("1", "2");
        assertEquals(Arrays.asList("1", "2"), userInfoProvider.getCurrentRoles());

        userService.setCurrentRoles(singletonList("1"));
        assertEquals(singletonList("1"), userInfoProvider.getCurrentRoles());
    }

    @Test
    public void testSetCurrentRolesNotAvailable()
    {
        initUserWithRoles("1", "2");
        userService.setCurrentRoles(singletonList("3"));
        assertEquals(singletonList("1"), userInfoProvider.getCurrentRoles());
    }

    @Test
    public void logout()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR);
        Request request = mock(Request.class);
        Response response = mock(Response.class);
        //userInfoProvider.setRequest(request)

        Session session = mock(Session.class);
        when(request.getSession(false)).thenReturn(session);

        userService.logout(request, response);

        verify(session).invalidate();
        assertEquals(RoleType.ROLE_GUEST, userInfoProvider.getUserName());
        assertEquals(singletonList(RoleType.ROLE_GUEST), userInfoProvider.getAvailableRoles());
    }

    @Test
    public void logoutAfterRemember()
    {
        ((ServerTestRequest) requestProvider.get())
                .setParameter(AbstractRememberMeService.DEFAULT_PARAMETER, "true");
        List<String> roles = Arrays.asList("1", "2");
        userService.saveUser("test", roles, roles, Locale.US, "192.168.0.1");

        Cookie cookie = ((ServerTestResponse) responseProvider.get()).getCookie(REMEMBER_ME_KEY);
        assertNotNull(cookie);
        assertEquals(REMEMBER_ME_KEY, cookie.getName());
        assertTrue(cookie.getMaxAge() > 0);

        ((ServerTestResponse) responseProvider.get()).clearCookies();
        userService.logout(requestProvider.get(), responseProvider.get());

        Cookie cookie2 = ((ServerTestResponse) responseProvider.get()).getCookie(REMEMBER_ME_KEY);
        assertNotNull(cookie2);
        assertEquals(REMEMBER_ME_KEY, cookie2.getName());
        assertEquals(0, cookie2.getMaxAge());
    }

    @Test
    public void log_saveAutoLoginUser()
    {
        Be5EventTestLogger.clearMock();
        when(RoleServiceMock.mock.getAvailableRoles("test")).thenReturn(singletonList("1"));
        when(RoleServiceMock.mock.getCurrentRoles("test")).thenReturn(singletonList("1"));
        userService.saveAutoLoginUser("test");
        verify(Be5EventTestLogger.mock).logCompleted(eq("UserService"), eq("saveAutoLoginUser"),
                any(), anyLong(), anyLong());
    }

    @Test
    public void logException_saveAutoLoginUser()
    {
        Be5EventTestLogger.clearMock();
        when(RoleServiceMock.mock.getAvailableRoles("test")).thenReturn(emptyList());
        when(RoleServiceMock.mock.getCurrentRoles("test")).thenReturn(emptyList());
        try {
            userService.saveAutoLoginUser("test");
        } catch (Throwable ignore) {}
        verify(Be5EventTestLogger.mock).logException(eq("UserService"), eq("saveAutoLoginUser"),
                any(), anyLong(), anyLong(), anyString());
    }
}
