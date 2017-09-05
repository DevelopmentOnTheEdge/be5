package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import org.junit.After;
import org.junit.Before;


public abstract class SqlMockOperationTest extends Be5ProjectTest
{
    @Inject protected OperationService operationService;
    @Inject protected DpsHelper dpsHelper;
    @Inject protected Meta meta;

    @Before
    public void beforeSqlMockOperationTest(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);
        SqlServiceMock.clearMock();
    }

    @After
    public void afterSqlMockOperationTest(){
        initUserWithRoles(RoleType.ROLE_GUEST);
    }

}
