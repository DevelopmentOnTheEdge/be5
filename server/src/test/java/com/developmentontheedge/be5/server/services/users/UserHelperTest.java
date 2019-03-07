package com.developmentontheedge.be5.server.services.users;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.security.UserInfo;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.web.Session;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class UserHelperTest extends ServerBe5ProjectTest
{
    private static UserInfo ui;
    @Inject
    private UserService userHelper;

    @Before
    public void setUpTestUser()
    {
        List<String> roles = Arrays.asList("1", "2");
        ui = userHelper.saveUser("test", roles, roles, Locale.US, "", false);

        assertEquals(roles, ui.getCurrentRoles());
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

        userHelper.setCurrentRoles(Collections.singletonList("1"));
        assertEquals(Collections.singletonList("1"), userInfoProvider.getCurrentRoles());
    }

    @Test
    public void testSetCurrentRolesNotAvailable()
    {
        initUserWithRoles("1", "2");
        userHelper.setCurrentRoles(Collections.singletonList("3"));
        assertEquals(Collections.singletonList("3"), userInfoProvider.getCurrentRoles());
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

        userHelper.logout(request, response);

        verify(session).invalidate();
        assertEquals(RoleType.ROLE_GUEST, userInfoProvider.getUserName());
        assertEquals(Collections.singletonList(RoleType.ROLE_GUEST), userInfoProvider.getAvailableRoles());
    }

}
