package com.developmentontheedge.be5.modules.core.services.impl;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDBTest;
import com.developmentontheedge.be5.modules.core.services.LoginService;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CryptoLoginServiceTest extends CoreBe5ProjectDBTest
{
    @Inject private DbService db;
    private LoginService loginService;
    private final String user_name = "CryptoLogin_test_user";

    @Before
    public void setUp()
    {
        loginService = new CryptoLoginService(db);
        if (database.getEntity("users").count(ImmutableMap.of("user_name", user_name)) == 0)
        {
            database.getEntity("users").add(ImmutableMap.of(
                    "user_name", user_name,
                    "user_pass", loginService.finalPassword("test_user_pass".toCharArray())
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
}
