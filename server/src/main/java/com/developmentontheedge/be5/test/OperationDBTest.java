package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.SqlService;
import javax.inject.Inject;
import com.developmentontheedge.be5.metadata.RoleType;
import org.junit.After;
import org.junit.Before;


public abstract class OperationDBTest extends Be5ProjectDBTest
{
    @Inject protected DpsHelper dpsHelper;
    @Inject protected Meta meta;
    @Inject protected SqlService db;

    @Before
    public void beforeSqlMockOperationTest()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);
    }

    @After
    public void afterSqlMockOperationTest()
    {
        initUserWithRoles(RoleType.ROLE_GUEST);
    }

}
