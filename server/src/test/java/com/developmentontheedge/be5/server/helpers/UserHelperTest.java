package com.developmentontheedge.be5.server.helpers;

import com.developmentontheedge.be5.security.UserInfo;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;


public class UserHelperTest extends ServerBe5ProjectTest
{
    private static UserInfo ui;
    @Inject
    private UserHelper userHelper;

    @Before
    public void setUpTestUser()
    {
        List<String> roles = Arrays.asList("1", "2");
        ui = userHelper.saveUser("test", roles, roles, Locale.US, "");

        assertEquals(roles, ui.getCurrentRoles());
    }

    @Test
    public void testGuestLocale()
    {
        initGuest();
        assertEquals("ru", userInfoProvider.getLanguage());
    }

}