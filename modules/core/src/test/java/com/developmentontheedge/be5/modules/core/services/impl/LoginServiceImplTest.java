package com.developmentontheedge.be5.modules.core.services.impl;

import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDBTest;
import com.developmentontheedge.be5.modules.core.services.LoginService;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LoginServiceImplTest extends CoreBe5ProjectDBTest
{
    @Inject private LoginService loginService;
    private final String user_name = "LoginServiceImpl_test_user";

    @Before
    public void init()
    {
        if (database.getEntity("users").count(ImmutableMap.of("user_name", user_name)) == 0)
        {
            database.getEntity("users").add(ImmutableMap.of(
                    "user_name", user_name,
                    "user_pass", "test_user_pass"
            ));
        }
    }

    @Test
    public void loginCheck()
    {
        assertTrue(loginService.loginCheck(user_name, "test_user_pass".toCharArray()));
    }

    @Test
    public void loginCheckErrorPass()
    {
        assertFalse(loginService.loginCheck(user_name, "error_pass".toCharArray()));
    }

    @Test
    public void loginCheckUserNotFound()
    {
        assertFalse(loginService.loginCheck("nonexistent_user", "pass".toCharArray()));
    }

    @Test
    public void testSetCurrentRoles()
    {
        initUserWithRoles("1", "2");
        assertEquals(Arrays.asList("1", "2"), userInfoProvider.get().getCurrentRoles());

        loginService.setCurrentRoles(Collections.singletonList("1"));
        assertEquals(Collections.singletonList("1"), userInfoProvider.get().getCurrentRoles());
    }

    @Test
    public void testSetCurrentRolesNotAvailable()
    {
        initUserWithRoles("1", "2");
        loginService.setCurrentRoles(Collections.singletonList("3"));
        assertEquals(Collections.singletonList("3"), userInfoProvider.get().getCurrentRoles());
    }

}
