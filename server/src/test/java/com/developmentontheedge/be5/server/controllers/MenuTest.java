package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import com.developmentontheedge.be5.web.Response;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class MenuTest extends ServerBe5ProjectTest
{
    @Inject private MenuController component;

    @Before
    public void init()
    {
        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    @Test
    public void test()
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest("/api/menu/"), response);

        verify(response).sendAsJson(isA(MenuController.MenuResponse.class));
    }

    @Test
    public void testWithIds()
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest("/api/menu/withIds"), response);

        verify(response).sendAsJson(isA(MenuController.MenuResponse.class));
    }

    @Test
    public void testUnknownAction()
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest("/api/menu/foo"), response);

        verify(response).sendErrorAsJson(eq("Unknown action"), eq(404));
    }

}
