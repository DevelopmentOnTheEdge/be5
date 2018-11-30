package com.developmentontheedge.be5.server.services.impl;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.server.model.UserInfoModel;
import com.developmentontheedge.be5.server.services.UserInfoModelService;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import org.junit.Test;

import javax.inject.Inject;

import java.util.Collections;

import static org.junit.Assert.*;

public class UserInfoModelServiceImplTest extends ServerBe5ProjectTest
{
    @Inject private UserInfoModelService userInfoModelService;

    @Test
    public void testGuest()
    {
        initGuest();
        UserInfoModel userInfoModel = userInfoModelService.getUserInfoModel();
        assertEquals(false, userInfoModel.isLoggedIn());
        assertEquals("Guest", userInfoModel.getUserName());
        assertEquals(Collections.singletonList(RoleType.ROLE_GUEST), userInfoModel.getAvailableRoles());
        assertEquals(Collections.singletonList(RoleType.ROLE_GUEST), userInfoModel.getCurrentRoles());
    }


    @Test
    public void testAdmin()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR);
        UserInfoModel userInfoModel = userInfoModelService.getUserInfoModel();
        assertEquals(true, userInfoModel.isLoggedIn());
        assertEquals("testUser", userInfoModel.getUserName());
        assertEquals(Collections.singletonList(RoleType.ROLE_ADMINISTRATOR), userInfoModel.getAvailableRoles());
        assertEquals(Collections.singletonList(RoleType.ROLE_ADMINISTRATOR), userInfoModel.getCurrentRoles());
    }
}
