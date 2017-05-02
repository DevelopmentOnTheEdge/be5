package com.developmentontheedge.be5.model;

import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.services.impl.LoginServiceImpl;
import com.developmentontheedge.be5.metadata.RoleType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class UserInfoTest extends AbstractProjectTest
{
    private static UserInfo ui;

    @Before
    public void setUpTestUser()
    {
        List<String> roles = Arrays.asList("1", "2");
        ui = sp.getLoginService().saveUser("test", roles, Locale.US);

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
        loginService.initGuest(null, sp);
        assertEquals(Collections.singletonList(RoleType.ROLE_GUEST), UserInfoHolder.getCurrentRoles());
        assertEquals(Collections.singletonList(RoleType.ROLE_GUEST), UserInfoHolder.getAvailableRoles());

        UserInfoHolder.getUserInfo().selectRoles(Collections.singletonList("Admin"));
        assertEquals(Collections.emptyList(), UserInfoHolder.getUserInfo().getCurrentRoles());
    }

    @Test
    public void testGuestLocale(){
        loginService.initGuest(null, sp);
        assertEquals("ru", UserInfoHolder.getLanguage());
    }

}
