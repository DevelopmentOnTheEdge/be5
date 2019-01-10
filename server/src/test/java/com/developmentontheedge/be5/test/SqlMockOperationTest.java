package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.server.helpers.DpsHelper;
import com.developmentontheedge.be5.test.mocks.DbServiceMock;
import org.junit.After;
import org.junit.Before;

import javax.inject.Inject;


public abstract class SqlMockOperationTest extends ServerBe5ProjectTest
{
    @Inject
    protected DpsHelper dpsHelper;
    @Inject
    protected Meta meta;

    @Before
    public void beforeSqlMockOperationTest()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);
        DbServiceMock.clearMock();
    }

    @After
    public void afterSqlMockOperationTest()
    {
        initUserWithRoles(RoleType.ROLE_GUEST);
    }

}
