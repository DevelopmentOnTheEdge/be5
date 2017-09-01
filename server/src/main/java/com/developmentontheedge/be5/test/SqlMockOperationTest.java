package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.helpers.SqlHelper;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;


public abstract class SqlMockOperationTest extends AbstractProjectTest
{
    @Inject protected OperationService operationService;
    @Inject protected SqlHelper sqlHelper;
    @Inject protected Meta meta;

    @BeforeClass
    public static void beforeClass(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);
    }

    @AfterClass
    public static void afterClass(){
        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    @Before
    public void before(){
        SqlServiceMock.clearMock();
    }

}
