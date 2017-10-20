package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.test.Be5ProjectTest;
import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.components.RoleSelector.RoleSelectorResponse;
import com.developmentontheedge.be5.metadata.RoleType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class RoleSelectorTest extends Be5ProjectTest
{
    @Inject private Injector injector;
    private static Component component;

    @Before
    public void init(){
        component = injector.getComponent("roleSelector");
    }


    @Test
    public void generate() throws Exception
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest(""), response, injector);

        verify(response).sendAsRawJson(eq(
                new RoleSelectorResponse(UserInfoHolder.getUserName(), Collections.emptyList(), Collections.emptyList())
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

        verify(response).sendAsRawJson(eq(
                new RoleSelectorResponse(UserInfoHolder.getUserName(), Collections.emptyList(), Collections.emptyList())
        ));
    }

    @Test
    public void generateSelectRolesAndSendEmpty() throws Exception
    {
        Response response = mock(Response.class);

        component.generate(getSpyMockRequest("", ImmutableMap.of("roles", "")),
                response, injector);

        verify(response).sendAsRawJson(eq(
                new RoleSelectorResponse(UserInfoHolder.getUserName(), Collections.emptyList(), Collections.emptyList())
        ));
    }

    @Test
    public void generateSelectRolesAndSendNewState() throws Exception
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        Response response = mock(Response.class);

        component.generate(getSpyMockRequest("select",
                ImmutableMap.of("roles", RoleType.ROLE_ADMINISTRATOR)), response, injector);

        RoleSelectorResponse roleSelectorResponse =
                new RoleSelectorResponse(UserInfoHolder.getUserName(),
                        ImmutableList.of(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER),
                        ImmutableList.of(RoleType.ROLE_ADMINISTRATOR));

        verify(response).sendAsRawJson(eq(roleSelectorResponse));

        initUserWithRoles(RoleType.ROLE_GUEST);
    }

}