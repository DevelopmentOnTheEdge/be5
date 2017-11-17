package com.developmentontheedge.be5.model;

import com.developmentontheedge.be5.api.services.LoginService;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.test.Be5ProjectTest;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.test.TestSession;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class UserInfoTest extends Be5ProjectTest
{
    private static UserInfo ui;
    @Inject private LoginService loginService;

    @Before
    public void setUpTestUser()
    {
        List<String> roles = Arrays.asList("1", "2");
        ui = loginService.saveUser("test", roles, Locale.US, "", new TestSession());

        assertEquals(roles, ui.getCurrentRoles());
    }

    @Test
    public void testSelectRoles(){
        ui.selectRoles(Collections.singletonList("1"));
        assertEquals(Collections.singletonList("1"), ui.getCurrentRoles());
    }

    @Test
    public void testSelectRolesNotAvailable()
    {
        ui.selectRoles(Collections.singletonList("3"));
        assertEquals(Collections.emptyList(), ui.getCurrentRoles());
    }

    @Test
    public void testGuestRoles(){
        loginService.initGuest(null);
        assertEquals(Collections.singletonList(RoleType.ROLE_GUEST), UserInfoHolder.getCurrentRoles());
        assertEquals(Collections.singletonList(RoleType.ROLE_GUEST), UserInfoHolder.getAvailableRoles());

        UserInfoHolder.getUserInfo().selectRoles(Collections.singletonList("Admin"));
        assertEquals(Collections.emptyList(), UserInfoHolder.getUserInfo().getCurrentRoles());
    }

    @Test
    public void testGuestLocale(){
        loginService.initGuest(null);
        assertEquals("ru", UserInfoHolder.getLanguage());
    }

}
