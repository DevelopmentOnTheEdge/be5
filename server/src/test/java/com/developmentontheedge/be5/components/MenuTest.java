package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.model.Action;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MenuTest extends AbstractProjectTest
{
    private static Component component;

    @BeforeClass
    public static void init(){
        initUserWithRoles(RoleType.ROLE_GUEST);
        component = injector.getComponent("menu");
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
    public void testDefaultAction()
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest("defaultAction"), response, injector);

        verify(response).sendAsRawJson(eq(new Action("call", "table/testtable/All records")));
    }

    @Test
    public void testDefaultActionAdmin()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        Response response = mock(Response.class);
        component.generate(getMockRequest("defaultAction"), response, injector);
        verify(response).sendAsRawJson(eq(new Action("call", "table/testtableAdmin/All records")));

        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    @Test
    public void testDefaultActionUser()
    {
        initUserWithRoles("User");

        Response response = mock(Response.class);
        component.generate(getMockRequest("defaultAction"), response, injector);
        verify(response).sendAsRawJson(eq(new Action("call", "table/testtUser/testtUser")));

        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    @Test
    public void testDefaultActionTestUser2()
    {
        initUserWithRoles("TestUser2");

        Response response = mock(Response.class);
        component.generate(getMockRequest("defaultAction"), response, injector);
        verify(response).sendAsRawJson(eq(new Action("call", "table/testtUser2/Test1")));

        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    @Test
    public void testUnknownAction()
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest("foo"), response, injector);

        verify(response).sendUnknownActionError();
    }

    @Test
    public void testGenerateSimpleMenu()
    {
        Menu menu = (Menu)component;
        Menu.MenuResponse menuResponse = menu.generateSimpleMenu(injector);

        assertEquals(true, menuResponse.loggedIn);

        assertEquals(1, menuResponse.root.size());
        assertEquals("testtable", menuResponse.root.get(0).title);
        assertEquals(new Action("call", "table/testtable/All records"), menuResponse.root.get(0).action);
        assertNull(menuResponse.root.get(0).children);
    }

    @Test
    public void testGenerateSimpleMenuAdmin()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        Menu menu = (Menu)component;
        Menu.MenuResponse menuResponse = menu.generateSimpleMenu(injector);
        assertEquals("Insert", menuResponse.root.get(1).operations.get(0).title);
        assertEquals(new Action("call", "form/testtableAdmin/All records/Insert"),
                menuResponse.root.get(1).operations.get(0).action);

        initUserWithRoles(RoleType.ROLE_GUEST);
    }



}
