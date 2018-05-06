package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.inject.Inject;
import com.developmentontheedge.be5.inject.Injector;
import com.developmentontheedge.be5.test.Be5ProjectTest;
import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.model.Action;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class MenuTest extends Be5ProjectTest
{
    @Inject private Injector injector;
    private Component component;

    @Before
    public void init()
    {
        initUserWithRoles(RoleType.ROLE_GUEST);
        component = (Component)injector.getComponent("menu");
    }

    @Test
    public void test()
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest(""), response, injector);

        verify(response).sendAsRawJson(isA(Menu.MenuResponse.class));
    }

    @Test
    public void testWithIds()
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest("withIds"), response, injector);

        verify(response).sendAsRawJson(isA(Menu.MenuResponse.class));
    }

    @Test
    public void testUnknownAction()
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest("foo"), response, injector);

        verify(response).sendUnknownActionError();
    }

}
