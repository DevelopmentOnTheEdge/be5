package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.components.UserInfoComponent.State;
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


import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class UserInfoComponentTest extends Be5ProjectTest
{
    @Inject private Injector injector;
    private static Component component;

    @Before
    public void init(){
        component = injector.getComponent("userInfo");
    }

    @Test
    public void generate() throws Exception
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest(""), response, injector);

        verify(response).sendAsRawJson(eq(new State(
                UserInfoHolder.isLoggedIn(),
                UserInfoHolder.getUserName(),
                UserInfoHolder.getAvailableRoles(),
                UserInfoHolder.getCurrentRoles()
        )));
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void generateSelectRolesAndSendNewStateNotAvailableRole() throws Exception
    {
        Response response = mock(Response.class);

        component.generate(getSpyMockRequest("", ImmutableMap.of("roles", RoleType.ROLE_ADMINISTRATOR)),
                response, injector);

        verify(response).sendAsRawJson(eq(
                new State(
                        UserInfoHolder.isLoggedIn(),
                        UserInfoHolder.getUserName(),
                        UserInfoHolder.getAvailableRoles(),
                        UserInfoHolder.getCurrentRoles()
                )
        ));
    }

    @Test
    public void generateSelectRolesAndSendEmpty() throws Exception
    {
        Response response = mock(Response.class);

        component.generate(getSpyMockRequest("", ImmutableMap.of("roles", "")),
                response, injector);

        verify(response).sendAsRawJson(eq(
                new State(
                        UserInfoHolder.isLoggedIn(),
                        UserInfoHolder.getUserName(),
                        UserInfoHolder.getAvailableRoles(),
                        UserInfoHolder.getCurrentRoles()
                )
        ));
    }

    @Test
    public void generateSelectRolesAndSendNewState() throws Exception
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        Response response = mock(Response.class);

        component.generate(getSpyMockRequest("selectRoles",
                ImmutableMap.of("roles", RoleType.ROLE_ADMINISTRATOR)), response, injector);

        verify(response).sendAsRawJson(eq(ImmutableList.of(RoleType.ROLE_ADMINISTRATOR)));

        initUserWithRoles(RoleType.ROLE_GUEST);
    }

}