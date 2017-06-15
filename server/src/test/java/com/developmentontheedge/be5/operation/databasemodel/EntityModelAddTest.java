package com.developmentontheedge.be5.operation.databasemodel;

import java.util.Arrays;
import java.util.Map;


import com.developmentontheedge.be5.env.Be5;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.operation.databasemodel.impl.DatabaseModel;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import com.google.common.collect.ImmutableMap;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.mockito.Mockito.verify;

public class EntityModelAddTest extends AbstractProjectTest
{
    private Injector sqlMockInjector = Be5.createInjector(new AbstractProjectTest.SqlMockBinder());
    private DatabaseModel database = sqlMockInjector.get(DatabaseModel.class);

    @BeforeClass
    public static void beforeClass(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);
    }

    @Before
    public void before(){
        SqlServiceMock.clearMock();
    }

    @AfterClass
    public static void afterClass(){
        initUserWithRoles(RoleType.ROLE_GUEST);
    }


    @Test
    public void testAdd()
    {
        EntityModel entity = database.getEntity( "testtableAdmin" );

        entity.add( ImmutableMap.of(
            "name", "Test",
            "value", "1"
        ));

        verify(SqlServiceMock.mock).insert("INSERT INTO testtableAdmin (name, value) VALUES (?, ?)",
                "Test", 1);
    }

    @Test
    public void testAddAll()
    {
        EntityModel entity = database.getEntity( "testtableAdmin" );

        java.util.List<Map<String, String>> list = Arrays.<Map<String, String>>asList(
                ImmutableMap.of(
                        "name", "Test",
                        "value", "1"
                ),
                ImmutableMap.of(
                        "name", "Test",
                        "value", "2"
                ),
                ImmutableMap.of(
                        "name", "Test",
                        "value", "3"
                )
        );

        entity.addAll( list );

        verify(SqlServiceMock.mock).insert("INSERT INTO testtableAdmin (name, value) VALUES (?, ?)","Test", 1);
        verify(SqlServiceMock.mock).insert("INSERT INTO testtableAdmin (name, value) VALUES (?, ?)","Test", 2);
        verify(SqlServiceMock.mock).insert("INSERT INTO testtableAdmin (name, value) VALUES (?, ?)","Test", 3);
    }

}
