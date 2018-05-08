package com.developmentontheedge.be5.model;

import com.google.inject.Inject;
import com.developmentontheedge.be5.test.Be5ProjectTest;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.test.TestSession;
import com.developmentontheedge.be5.api.helpers.UserHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;


public class UserInfoTest extends Be5ProjectTest
{
    private static UserInfo ui;
    @Inject private UserHelper userHelper;

    @Before
    public void setUpTestUser()
    {
        List<String> roles = Arrays.asList("1", "2");
        ui = userHelper.saveUser("test", roles, roles, Locale.US, "", new TestSession());

        assertEquals(roles, ui.getCurrentRoles());
    }

    @Test
    public void testGuestLocale()
    {
        initGuest();
        assertEquals("ru", UserInfoHolder.getLanguage());
    }

}
