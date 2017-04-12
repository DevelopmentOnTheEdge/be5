package com.developmentontheedge.be5.model;

import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.api.services.impl.LoginServiceImpl;
import com.developmentontheedge.be5.api.services.impl.ProjectProviderImpl;
import com.developmentontheedge.be5.metadata.RoleType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class UserInfoTest
{
    private static ProjectProvider projectProvider;
    private static LoginServiceImpl loginService;
    private static UserInfo ui;
    @BeforeClass
    public static void setUp()
    {
        projectProvider = new ProjectProviderImpl()
        {
            @Override
            protected Path findProjectPath()
            {
                return Paths.get("src/test/resources/app").toAbsolutePath();
            }
        };
        loginService = new LoginServiceImpl(null, null, projectProvider);
    }

    @Before
    public void setUpTestUser()
    {
        String username = "test";
        ArrayList<String> roles = new ArrayList<>();
        roles.add("1");roles.add("2");

        ui = new UserInfo(username, new Date());

        ui.setCurrentRoles( new ArrayList<>(roles));
        ui.setAvailableRoles( new ArrayList<>(roles));

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
    }

    @Test
    public void testGuestLocale(){
        loginService.initGuest(null);
        assertEquals("ru", UserInfoHolder.getLanguage());
    }

}
