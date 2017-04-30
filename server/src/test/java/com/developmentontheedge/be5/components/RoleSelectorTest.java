package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.AbstractProjectTest;
import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.exceptions.Be5ErrorCode;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.exceptions.ErrorMessages;
import com.developmentontheedge.be5.metadata.RoleType;
import com.google.common.collect.ImmutableList;
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
        component = loadedClasses.get("roleSelector");
    }


    @Test
    public void generate() throws Exception
    {
        Response response = mock(Response.class);

        component.generate(getMockRequestWithUri(""), response, sp);

        RoleSelector.RoleSelectorResponse roleSelectorResponse =
                new RoleSelector.RoleSelectorResponse(ImmutableList.of(RoleType.ROLE_GUEST), ImmutableList.of(RoleType.ROLE_GUEST));

        verify(response).sendAsRawJson(eq(roleSelectorResponse));
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void generateParameterIsAbsent() throws Exception
    {
        expectedEx.expect(Be5Exception.class);
        expectedEx.expectMessage(ErrorMessages.formatMessage(Be5ErrorCode.PARAMETER_ABSENT, "roles"));

        component.generate(getSpyMockRequestWithUri("select"), mock(Response.class), sp);
    }

    @Test
    public void generateSelectRolesAndSendNewStateNotAvailableRole() throws Exception
    {
        Response response = mock(Response.class);

        component.generate(getSpyMockRequestWithUriAndParams("",
                "roles", RoleType.ROLE_ADMINISTRATOR), response, sp);

        RoleSelector.RoleSelectorResponse roleSelectorResponse =
                new RoleSelector.RoleSelectorResponse(ImmutableList.of(RoleType.ROLE_GUEST),
                                                      ImmutableList.of(RoleType.ROLE_GUEST));

        verify(response).sendAsRawJson(eq(roleSelectorResponse));
    }

    @Test
    public void generateSelectRolesAndSendNewState() throws Exception
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        Response response = mock(Response.class);

        component.generate(getSpyMockRequestWithUriAndParams("select",
                "roles", RoleType.ROLE_ADMINISTRATOR), response, sp);

        RoleSelector.RoleSelectorResponse roleSelectorResponse =
                new RoleSelector.RoleSelectorResponse(
                        ImmutableList.of(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER),
                        ImmutableList.of(RoleType.ROLE_ADMINISTRATOR));

        verify(response).sendAsRawJson(eq(roleSelectorResponse));

        initUserWithRoles(RoleType.ROLE_GUEST);
    }

}