package com.developmentontheedge.be5.modules.core.services;

import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDbMockTest;
import com.developmentontheedge.be5.server.authentication.RoleService;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class RoleHelperTest extends CoreBe5ProjectDbMockTest
{
    @Inject private RoleService roleService;

    @Test
    public void parseRoles()
    {
        assertEquals(Arrays.asList("1", "2"), ((RoleServiceImpl) roleService).parseRoles("('1','2')"));
        assertEquals(Collections.emptyList(), ((RoleServiceImpl) roleService).parseRoles("()"));
        assertEquals(Collections.emptyList(), ((RoleServiceImpl) roleService).parseRoles(null));
    }

    @Test
    public void getAvailableCurrentRoles()
    {
        assertEquals(singletonList("1"),
                roleService.getAvailableCurrentRoles(Arrays.asList("1", "2"), singletonList("1")));
    }

    @Test
    public void getAvailableCurrentRolesEmptyNewRoles()
    {
        assertEquals(singletonList("1"),
                roleService.getAvailableCurrentRoles(Collections.emptyList(), singletonList("1")));
    }

    @Test
    public void testSetCurrentRolesNotAvailable()
    {
        assertEquals(singletonList("1"),
                roleService.getAvailableCurrentRoles(singletonList("3"), Arrays.asList("1", "2")));
    }

}
