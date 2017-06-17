package com.developmentontheedge.be5.operation.databasemodel.impl;

import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.env.Be5;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.operation.databasemodel.EntityModel;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.verify;

public class EntityModelBaseTest extends AbstractProjectTest
{
    private DatabaseModel database = sqlMockInjector.get(DatabaseModel.class);
    private EntityModel testTableAdmin = database.getEntity("testtableAdmin");

    @BeforeClass
    public static void beforeClass()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);
    }

    @AfterClass
    public static void afterClass()
    {
        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    @Before
    public void before()
    {
        //SqlServiceMock.clearMock();
    }

    @Test
    @Ignore
    public void count() throws Exception {
        //testTableAdmin.count();
        //verify(SqlServiceMock.mock).update("");
    }

}