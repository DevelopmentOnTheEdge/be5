package com.developmentontheedge.be5.databasemodel;

import java.util.Arrays;
import java.util.Map;


import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import com.google.common.collect.ImmutableMap;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertEquals;

public class EntityModelAddTest extends AbstractProjectTest
{
    @Inject private DatabaseModel database;

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
        when(SqlServiceMock.mock.insert(anyString(), anyVararg())).thenReturn("1");

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
        when(SqlServiceMock.mock.insert(anyString(), anyVararg())).thenReturn("1");

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

    @Test
    public void testReturnValue()
    {
        EntityModel entity = database.getEntity( "testtableAdmin" );

        when(SqlServiceMock.mock.insert(anyString(), anyVararg())).thenReturn("1");
        String id = entity.add( ImmutableMap.of("name", "Test","value", "1"));
        assertEquals("1", id);

        when(SqlServiceMock.mock.insert(anyString(), anyVararg())).thenReturn(2L);
        id = entity.add( ImmutableMap.of("name", "Test","value", "1"));
        assertEquals("2", id);
    }

}
