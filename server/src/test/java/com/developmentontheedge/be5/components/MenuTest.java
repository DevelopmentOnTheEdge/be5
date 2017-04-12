package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.AbstractProjectTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MenuTest extends AbstractProjectTest
{
    @Test
    public void test(){
        getServiceProvider().getLoginService().initGuest(null);
        Menu menu = new Menu();
        Menu.MenuResponse menuResponse = menu.generateSimpleMenu(getServiceProvider());
        assertEquals("testtable", menuResponse.root.get(0).title);
        assertNotNull(menuResponse.root.get(0).children);
        assertEquals("call", menuResponse.root.get(0).children.get(0).action.name);
        assertEquals("table/testtable/Test 1D unknown", menuResponse.root.get(0).children.get(0).action.arg);
    }

}
