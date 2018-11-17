package com.developmentontheedge.be5.modules.core.operations.system.daemons;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDBTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ExecuteInOperationTest extends CoreBe5ProjectDBTest
{
    @Before
    public void setUp()
    {
        initUserWithRoles(RoleType.ROLE_SYSTEM_DEVELOPER);
    }

    @Test
    public void testGet()
    {
        long before = db.countFrom("users WHERE user_name LIKE 'TestDaemonUser%'");
        executeOperation("_system_", "Daemons", "ExecuteInOperation", "TestDaemon", "").getSecond();

        assertTrue(db.countFrom("users WHERE user_name LIKE 'TestDaemonUser%'") > before);
    }
}
