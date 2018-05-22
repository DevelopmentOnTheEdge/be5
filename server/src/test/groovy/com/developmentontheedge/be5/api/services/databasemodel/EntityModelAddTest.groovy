package com.developmentontheedge.be5.api.services.databasemodel

import javax.inject.Inject
import com.developmentontheedge.be5.metadata.RoleType
import com.developmentontheedge.be5.test.ServerBe5ProjectTest
import com.developmentontheedge.be5.test.mocks.DbServiceMock
import org.junit.After
import org.junit.Before
import org.junit.Test

import static org.mockito.Matchers.anyString
import static org.mockito.Matchers.anyVararg
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

import static org.junit.Assert.assertEquals

class EntityModelAddTest extends ServerBe5ProjectTest
{
    @Inject private DatabaseModel database

    @Before
    void beforeClass(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER)
    }

    @After
    void afterClass(){
        initUserWithRoles(RoleType.ROLE_GUEST)
    }

    @Before
    void before(){
        DbServiceMock.clearMock()
    }

    @Test
    void testAdd()
    {
        EntityModel entity = database.getEntity( "testtableAdmin" )
        when(DbServiceMock.mock.insert(anyString(), anyVararg())).thenReturn("1")

        entity.add([
            name : "Test",
            value: 1
        ])

        verify(DbServiceMock.mock).insert("INSERT INTO testtableAdmin (name, value) VALUES (?, ?)",
                "Test", 1)
    }

    @Test
    void testAddAll()
    {
        EntityModel entity = database.getEntity( "testtableAdmin" )
        when(DbServiceMock.mock.insert(anyString(), anyVararg())).thenReturn("1")

        def list = [
                [
                        name : "Test",
                        value: 1
                ],
                [
                        name : "Test",
                        value: 2
                ],
                [
                        name : "Test",
                        value: 3
                ],
        ]

        entity.addAll( list )

        verify(DbServiceMock.mock).insert("INSERT INTO testtableAdmin (name, value) VALUES (?, ?)","Test", 1)
        verify(DbServiceMock.mock).insert("INSERT INTO testtableAdmin (name, value) VALUES (?, ?)","Test", 2)
        verify(DbServiceMock.mock).insert("INSERT INTO testtableAdmin (name, value) VALUES (?, ?)","Test", 3)
    }

    @Test
    void testReturnValue()
    {
        EntityModel entity = database.getEntity( "testtableAdmin" )

        when(DbServiceMock.mock.insert(anyString(), anyVararg())).thenReturn("1")
        String id = entity.add( [
                name : "Test",
                value: "1"
        ])
        assertEquals("1", id)

        when(DbServiceMock.mock.insert(anyString(), anyVararg())).thenReturn(2L)
        id = entity.add( [
                name : "Test",
                value: "1"
        ])
        assertEquals("2", id)
    }

}
