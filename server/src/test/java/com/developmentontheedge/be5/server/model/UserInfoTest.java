package com.developmentontheedge.be5.server.model;

import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.server.helpers.UserHelper;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;


public class UserInfoTest extends ServerBe5ProjectTest
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
        assertEquals("ru", userInfoProvider.get().getLanguage());
    }

}
