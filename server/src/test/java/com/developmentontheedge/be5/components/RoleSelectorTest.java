package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.exceptions.Be5ErrorCode;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.exceptions.ErrorMessages;
import com.developmentontheedge.be5.components.RoleSelector.RoleSelectorResponse;
import com.developmentontheedge.be5.metadata.RoleType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RoleSelectorTest extends AbstractProjectTest
{
    private static Component component;

    @BeforeClass
    public static void init(){
        component = injector.getComponent("roleSelector");
    }


    @Test
    public void generate() throws Exception
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest(""), response, sp);

        RoleSelectorResponse roleSelectorResponse =
                new RoleSelectorResponse(ImmutableList.of(RoleType.ROLE_GUEST), ImmutableList.of(RoleType.ROLE_GUEST));

        verify(response).sendAsRawJson(eq(roleSelectorResponse));
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void generateParameterIsAbsent() throws Exception
    {
        expectedEx.expect(Be5Exception.class);
        expectedEx.expectMessage(ErrorMessages.formatMessage(Be5ErrorCode.PARAMETER_ABSENT, "roles"));

        component.generate(getSpyMockRequest("select"), mock(Response.class), sp);
    }

    @Test
    public void generateSelectRolesAndSendNewStateNotAvailableRole() throws Exception
    {
        Response response = mock(Response.class);

        component.generate(getSpyMockRequest("", ImmutableMap.of("roles", RoleType.ROLE_ADMINISTRATOR)),
                response, sp);

        RoleSelectorResponse roleSelectorResponse =
                new RoleSelectorResponse(ImmutableList.of(RoleType.ROLE_GUEST),
                                                      ImmutableList.of(RoleType.ROLE_GUEST));

        verify(response).sendAsRawJson(eq(roleSelectorResponse));
    }

    @Test
    public void generateSelectRolesAndSendNewState() throws Exception
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        Response response = mock(Response.class);

        component.generate(getSpyMockRequest("select",
                ImmutableMap.of("roles", RoleType.ROLE_ADMINISTRATOR)), response, sp);

        RoleSelectorResponse roleSelectorResponse =
                new RoleSelectorResponse(
                        ImmutableList.of(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER),
                        ImmutableList.of(RoleType.ROLE_ADMINISTRATOR));

        verify(response).sendAsRawJson(eq(roleSelectorResponse));

        initUserWithRoles(RoleType.ROLE_GUEST);
    }

}