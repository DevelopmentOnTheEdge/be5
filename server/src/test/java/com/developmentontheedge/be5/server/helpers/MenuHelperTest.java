package com.developmentontheedge.be5.server.helpers;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.server.model.Action;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import org.junit.Test;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class MenuHelperTest extends ServerBe5ProjectTest
{
    @Inject
    private MenuHelper menuHelper;

    @Test
    public void testGenerateSimpleMenu()
    {
        initUserWithRoles("User");

        List<MenuHelper.RootNode> nodes = menuHelper.collectEntities(false, EntityType.TABLE);

        assertEquals("testtable", nodes.get(1).getTitle());
        assertEquals(new Action("call", "table/testtable/All records"), nodes.get(1).getAction());
        assertNull(nodes.get(1).getChildren());
    }

    @Test
    public void testGenerateSimpleMenuAdmin()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        List<MenuHelper.RootNode> nodes = menuHelper.collectEntities(false, EntityType.TABLE);

        assertEquals("Добавить", nodes.get(1).getOperations().get(0).title);
        assertEquals(new Action("call", "form/dateTime/All records/Insert"),
                nodes.get(1).getOperations().get(0).action);

        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    @Test
    public void testRootMenu()
    {
        initUserWithRoles("TestUser2");

        List<MenuHelper.RootNode> nodes = menuHelper.collectEntities(false, EntityType.TABLE);

        assertEquals(new Action("call", "table/rootMenu/Test1"),
                nodes.get(1).getAction());
        assertEquals(null, nodes.get(1).getChildren());

        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    @Test
    public void testDefaultAction()
    {
        initUserWithRoles(RoleType.ROLE_GUEST);

        assertEquals(new Action("call", "table/testtable/All records"),
                menuHelper.getDefaultAction());
    }

    @Test
    public void testDefaultActionAdmin()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        assertEquals(new Action("call", "table/testtableAdmin/All records"),
                menuHelper.getDefaultAction());
    }

    @Test
    public void testDefaultActionUser()
    {
        initUserWithRoles("User");

        assertEquals(new Action("call", "table/testtUser/testtUser"),
                menuHelper.getDefaultAction());
    }

    @Test
    public void testDefaultActionTestUser2()
    {
        initUserWithRoles("TestUser2");

        assertEquals(new Action("call", "table/atest/Test1"),
                menuHelper.getDefaultAction());
    }

}