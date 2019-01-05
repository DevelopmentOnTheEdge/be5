package com.developmentontheedge.be5.modules.core.controllers;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDbMockTest;
import com.developmentontheedge.be5.server.model.UserInfoModel;
import com.developmentontheedge.be5.test.mocks.DbServiceMock;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;

import static com.developmentontheedge.be5.metadata.RoleType.ROLE_ADMINISTRATOR;
import static com.developmentontheedge.be5.metadata.RoleType.ROLE_SYSTEM_DEVELOPER;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class UserInfoControllerTest extends CoreBe5ProjectDbMockTest
{
    @Inject
    private UserInfoController component;

    @Before
    public void init()
    {
        initUserWithRoles(ROLE_ADMINISTRATOR);
    }

    @Test
    public void generate()
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest("/api/userInfo/"), response);

        verify(response).sendAsJson(new UserInfoModel(
                true,
                TEST_USER,
                Collections.singletonList(ROLE_ADMINISTRATOR),
                Collections.singletonList(ROLE_ADMINISTRATOR),
                any(Instant.class),
                ""));
    }

    @Test
    public void testGuest()
    {
        initGuest();
        Response response = mock(Response.class);

        component.generate(getMockRequest("/api/userInfo/"), response);

        verify(response).sendAsJson(new UserInfoModel(
                false,
                RoleType.ROLE_GUEST,
                Collections.singletonList(RoleType.ROLE_GUEST),
                Collections.singletonList(RoleType.ROLE_GUEST),
                any(Instant.class),
                ""));
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void generateSelectRolesAndSendNewStateNotAvailableRole() throws Exception
    {
        Response response = mock(Response.class);

        component.generate(getSpyMockRequest("/api/userInfo/", ImmutableMap.of("roles", ROLE_ADMINISTRATOR)),
                response);

        verify(response).sendAsJson(new UserInfoModel(
                true,
                TEST_USER,
                Collections.singletonList(ROLE_ADMINISTRATOR),
                Collections.singletonList(ROLE_ADMINISTRATOR),
                any(Instant.class),
                ""));
    }

    @Test
    public void generateSelectRolesAndSendEmpty() throws Exception
    {
        Response response = mock(Response.class);

        component.generate(getSpyMockRequest("/api/userInfo/", ImmutableMap.of("roles", "")),
                response);

        verify(response).sendAsJson(new UserInfoModel(
                true,
                TEST_USER,
                Collections.singletonList(ROLE_ADMINISTRATOR),
                Collections.singletonList(ROLE_ADMINISTRATOR),
                any(Instant.class),
                ""));
    }

    @Test
    public void generateSelectRolesAndSendNewState() throws Exception
    {
        DbServiceMock.clearMock();
        Response response = mock(Response.class);

        component.generate(getSpyMockRequest("/api/userInfo/selectRoles",
                ImmutableMap.of("roles", ROLE_ADMINISTRATOR)), response);

        verify(response).sendAsJson(eq(ImmutableList.of(ROLE_ADMINISTRATOR)));

        verify(DbServiceMock.mock).update("UPDATE user_prefs SET pref_value = ? WHERE pref_name = ? AND user_name = ?",
                "('Administrator')",
                "current-role-list",
                "testUser");

        verify(DbServiceMock.mock).insert("INSERT INTO user_prefs VALUES( ?, ?, ? )",
                "testUser",
                "current-role-list",
                "('Administrator')");

        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    @Test
    public void testSetCurrentRolesNotAvailable()
    {
        Request request = getSpyMockRequest("/api/userInfo/selectRoles",
                ImmutableMap.of("roles", ROLE_ADMINISTRATOR + "," + ROLE_SYSTEM_DEVELOPER));
        component.generate(request);

        assertEquals(Collections.singletonList(ROLE_ADMINISTRATOR), userInfoProvider.getCurrentRoles());
    }

    @Test
    public void testSetCurrentRolesEmpty()
    {
        Request request = getSpyMockRequest("/api/userInfo/selectRoles",
                ImmutableMap.of("roles", ""));
        component.generate(request);

        assertEquals(Collections.singletonList(ROLE_ADMINISTRATOR), userInfoProvider.getCurrentRoles());
    }

}
