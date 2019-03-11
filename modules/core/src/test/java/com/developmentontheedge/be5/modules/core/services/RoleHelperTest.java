package com.developmentontheedge.be5.modules.core.services;

import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDbMockTest;
import com.developmentontheedge.be5.server.authentication.RoleService;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class RoleHelperTest extends CoreBe5ProjectDbMockTest
{
    @Inject private RoleService roleHelper;

    @Test
    public void parseRoles()
    {
        assertEquals(Arrays.asList("1", "2"), ((RoleServiceImpl)roleHelper).parseRoles("('1','2')"));
        assertEquals(Collections.emptyList(), ((RoleServiceImpl)roleHelper).parseRoles("()"));
        assertEquals(Collections.emptyList(), ((RoleServiceImpl)roleHelper).parseRoles(null));
    }
}
