package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.security.UserInfoHolder;
import com.developmentontheedge.be5.server.model.UserInfoModel;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import com.developmentontheedge.be5.test.mocks.RoleServiceMock;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
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
import static com.google.common.collect.ImmutableList.of;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class UserInfoControllerTest extends ServerBe5ProjectTest
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
    public void generateSelectRolesAndSendNewState()
    {
        RoleServiceMock.clearMock();
        initUserWithRoles(ROLE_ADMINISTRATOR, ROLE_SYSTEM_DEVELOPER);

        assertEquals(of(ROLE_ADMINISTRATOR, ROLE_SYSTEM_DEVELOPER),
                UserInfoHolder.getLoggedUser().getCurrentRoles());

        UserInfoModel userInfoModel = component.generate(getSpyMockRequest("/api/userInfo/selectRoles",
                ImmutableMap.of("roles", ROLE_ADMINISTRATOR)), "selectRoles");

        assertEquals(of(ROLE_ADMINISTRATOR), userInfoModel.getCurrentRoles());

        verify(RoleServiceMock.mock).updateCurrentRoles("testUser", of(ROLE_ADMINISTRATOR));

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
