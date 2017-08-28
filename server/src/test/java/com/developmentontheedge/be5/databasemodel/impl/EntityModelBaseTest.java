package com.developmentontheedge.be5.databasemodel.impl;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.databasemodel.EntityModel;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import static org.mockito.Mockito.verify;

public class EntityModelBaseTest extends AbstractProjectTest
{
    private DatabaseModel database = injector.get(DatabaseModel.class);
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

}