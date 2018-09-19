package com.developmentontheedge.be5.modules.core.services;

import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDbMockTest;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class RoleHelperTest extends CoreBe5ProjectDbMockTest
{
    @Inject private RoleHelper roleHelper;

    @Test
    public void parseRoles()
    {
        assertEquals(Arrays.asList("1", "2"), roleHelper.parseRoles("('1','2')"));
        assertEquals(Collections.emptyList(), roleHelper.parseRoles("()"));
        assertEquals(Collections.emptyList(), roleHelper.parseRoles(null));
    }
}
