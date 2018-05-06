package com.developmentontheedge.be5.modules.core.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.inject.Inject;
import com.developmentontheedge.be5.inject.Injector;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.modules.core.model.UserInfoModel;
import com.developmentontheedge.be5.test.Be5ProjectTest;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


import java.time.Instant;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class UserInfoComponentTest extends Be5ProjectTest
{
    @Inject private Injector injector;
    private static Component component;

    @Before
    public void init()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR);
        component = (Component)injector.getComponent("userInfo");
    }

    @Test
    public void generate()
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest(""), response, injector);

        verify(response).sendAsRawJson(new UserInfoModel(
                true,
                TEST_USER,
                Collections.singletonList(RoleType.ROLE_ADMINISTRATOR),
                Collections.singletonList(RoleType.ROLE_ADMINISTRATOR),
                any(Instant.class),
                ""));
    }

    @Test
    public void testGuest()
    {
        initGuest();
        Response response = mock(Response.class);

        component.generate(getMockRequest(""), response, injector);

        verify(response).sendAsRawJson(new UserInfoModel(
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

        component.generate(getSpyMockRequest("", ImmutableMap.of("roles", RoleType.ROLE_ADMINISTRATOR)),
                response, injector);

        verify(response).sendAsRawJson(new UserInfoModel(
                true,
                TEST_USER,
                Collections.singletonList(RoleType.ROLE_ADMINISTRATOR),
                Collections.singletonList(RoleType.ROLE_ADMINISTRATOR),
                any(Instant.class),
                ""));
    }

    @Test
    public void generateSelectRolesAndSendEmpty() throws Exception
    {
        Response response = mock(Response.class);

        component.generate(getSpyMockRequest("", ImmutableMap.of("roles", "")),
                response, injector);

        verify(response).sendAsRawJson(new UserInfoModel(
                true,
                TEST_USER,
                Collections.singletonList(RoleType.ROLE_ADMINISTRATOR),
                Collections.singletonList(RoleType.ROLE_ADMINISTRATOR),
                any(Instant.class),
                ""));
    }

    @Test
    public void generateSelectRolesAndSendNewState() throws Exception
    {
        Response response = mock(Response.class);

        component.generate(getSpyMockRequest("selectRoles",
                ImmutableMap.of("roles", RoleType.ROLE_ADMINISTRATOR)), response, injector);

        verify(response).sendAsRawJson(eq(ImmutableList.of(RoleType.ROLE_ADMINISTRATOR)));

        verify(SqlServiceMock.mock).update("UPDATE user_prefs SET pref_value = ? WHERE pref_name = ? AND user_name = ?",
                "('Administrator')",
                "current-role-list",
                "testUser");

        verify(SqlServiceMock.mock).insert("INSERT INTO user_prefs VALUES( ?, ?, ? )",
                "testUser",
                "current-role-list",
                "('Administrator')");

        initUserWithRoles(RoleType.ROLE_GUEST);
    }

}