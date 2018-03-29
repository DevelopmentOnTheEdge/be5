package com.developmentontheedge.be5.modules.core.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.services.LoginService;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.test.Be5ProjectTest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


import java.util.Collections;
import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class UserInfoComponentTest extends Be5ProjectTest
{
    @Inject private Injector injector;
    @Inject private LoginService loginService;
    private static Component component;

    @Before
    public void init()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR);
        component = injector.getComponent("userInfo");
    }

    @Test
    public void generate() throws Exception
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest(""), response, injector);

        verify(response).sendAsRawJson(new UserInfoComponent.State(
                true,
                TEST_USER,
                Collections.singletonList(RoleType.ROLE_ADMINISTRATOR),
                Collections.singletonList(RoleType.ROLE_ADMINISTRATOR),
                any(Date.class)
        ));
    }

    @Test
    public void testGuest() throws Exception
    {
        loginService.initGuest(null);
        Response response = mock(Response.class);

        component.generate(getMockRequest(""), response, injector);

        verify(response).sendAsRawJson(new UserInfoComponent.State(
                false,
                RoleType.ROLE_GUEST,
                Collections.singletonList(RoleType.ROLE_GUEST),
                Collections.singletonList(RoleType.ROLE_GUEST),
                any(Date.class)
        ));
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void generateSelectRolesAndSendNewStateNotAvailableRole() throws Exception
    {
        Response response = mock(Response.class);

        component.generate(getSpyMockRequest("", ImmutableMap.of("roles", RoleType.ROLE_ADMINISTRATOR)),
                response, injector);

        verify(response).sendAsRawJson(new UserInfoComponent.State(
                true,
                TEST_USER,
                Collections.singletonList(RoleType.ROLE_ADMINISTRATOR),
                Collections.singletonList(RoleType.ROLE_ADMINISTRATOR),
                any(Date.class)
        ));
    }

    @Test
    public void generateSelectRolesAndSendEmpty() throws Exception
    {
        Response response = mock(Response.class);

        component.generate(getSpyMockRequest("", ImmutableMap.of("roles", "")),
                response, injector);

        verify(response).sendAsRawJson(new UserInfoComponent.State(
                true,
                TEST_USER,
                Collections.singletonList(RoleType.ROLE_ADMINISTRATOR),
                Collections.singletonList(RoleType.ROLE_ADMINISTRATOR),
                any(Date.class)
        ));
    }

    @Test
    public void generateSelectRolesAndSendNewState() throws Exception
    {
        Response response = mock(Response.class);

        component.generate(getSpyMockRequest("selectRoles",
                ImmutableMap.of("roles", RoleType.ROLE_ADMINISTRATOR)), response, injector);

        verify(response).sendAsRawJson(eq(ImmutableList.of(RoleType.ROLE_ADMINISTRATOR)));

        initUserWithRoles(RoleType.ROLE_GUEST);
    }

}