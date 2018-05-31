package com.developmentontheedge.be5.server.controllers;

import javax.inject.Inject;

import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.metadata.RoleType;
import org.junit.Before;
import org.junit.Test;

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

        component.generate(getMockRequest(""), response);

        verify(response).sendAsRawJson(isA(MenuController.MenuResponse.class));
    }

    @Test
    public void testWithIds()
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest("withIds"), response);

        verify(response).sendAsRawJson(isA(MenuController.MenuResponse.class));
    }

    @Test
    public void testUnknownAction()
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest("foo"), response);

        verify(response).sendUnknownActionError();
    }

}
