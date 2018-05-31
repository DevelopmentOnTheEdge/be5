package com.developmentontheedge.be5.server.model;

import javax.inject.Inject;

import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import com.developmentontheedge.be5.server.servlet.UserInfoHolder;
import com.developmentontheedge.be5.test.mocks.ServerTestSession;
import com.developmentontheedge.be5.server.helpers.UserHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;


public class UserInfoTest extends ServerBe5ProjectTest
{
    private static UserInfo ui;
    @Inject private UserHelper userHelper;

    @Before
    public void setUpTestUser()
    {
        List<String> roles = Arrays.asList("1", "2");
        ui = userHelper.saveUser("test", roles, roles, Locale.US, "", new ServerTestSession());

        assertEquals(roles, ui.getCurrentRoles());
    }

    @Test
    public void testGuestLocale()
    {
        initGuest();
        assertEquals("ru", UserInfoHolder.getLanguage());
    }

}
