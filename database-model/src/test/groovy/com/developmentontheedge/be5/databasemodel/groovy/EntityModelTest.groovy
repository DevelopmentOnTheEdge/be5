package com.developmentontheedge.be5.databasemodel.groovy

import com.developmentontheedge.be5.database.sql.ResultSetParser
import com.developmentontheedge.be5.databasemodel.DatabaseModel
import com.developmentontheedge.be5.databasemodel.DatabaseModelSqlMockProjectTest
import com.developmentontheedge.be5.databasemodel.EntityModel
import com.developmentontheedge.be5.test.mocks.DbServiceMock
import com.developmentontheedge.beans.DynamicPropertySet
import org.junit.Before
import org.junit.Test
import org.mockito.Matchers

import javax.inject.Inject

import static org.junit.Assert.assertEquals
import static org.mockito.Matchers.*
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

class EntityModelTest extends DatabaseModelSqlMockProjectTest
{
    @Inject
    private DatabaseModel database

    @Before
    void before()
    {
        DbServiceMock.clearMock()
    }

    @Test
    void testAdd()
    {
        EntityModel entity = database.getEntity("testtableAdmin")
        when(DbServiceMock.mock.insertRaw(anyString(), anyVararg())).thenReturn("1")

        entity.add([
                name : "Test",
                valueCol: 1
        ])

        verify(DbServiceMock.mock).insertRaw("INSERT INTO testtableAdmin (name, valueCol) VALUES (?, ?)",
                "Test", 1)
    }

    @Test
    void testAddAll()
    {
        EntityModel entity = database.getEntity("testtableAdmin")
        when(DbServiceMock.mock.insert(anyString(), anyVararg())).thenReturn("1")

        def list = [
                [
                        name : "Test",
                        valueCol: 1
                ],
                [
                        name : "Test",
                        valueCol: 2
                ],
                [
                        name : "Test",
                        valueCol: 3
                ],
        ]

        entity.addAll(list)

        verify(DbServiceMock.mock).insertRaw("INSERT INTO testtableAdmin (name, valueCol) VALUES (?, ?)", "Test", 1)
        verify(DbServiceMock.mock).insertRaw("INSERT INTO testtableAdmin (name, valueCol) VALUES (?, ?)", "Test", 2)
        verify(DbServiceMock.mock).insertRaw("INSERT INTO testtableAdmin (name, valueCol) VALUES (?, ?)", "Test", 3)
    }

    @Test
    void testReturnValue()
    {
        EntityModel entity = database.getEntity("testtableAdmin")

        when(DbServiceMock.mock.insertRaw(anyString(), anyVararg())).thenReturn("1")
        String id = entity.add([
                name : "Test",
                valueCol: "1"
        ])
        assertEquals("1", id)

        when(DbServiceMock.mock.insertRaw(anyString(), anyVararg())).thenReturn(2L)
        id = entity.add([
                name : "Test",
                valueCol: "1"
        ])
        assertEquals("2", id)
    }

    @Test
    void simpleMockTestExample() throws Exception
    {
        when(DbServiceMock.mock.select(anyString(),
                Matchers.<ResultSetParser<DynamicPropertySet>> any(), eq(4444L))).thenReturn(getDpsS([
                ID   : 4444L,
                name : "test",
                valueCol: 123
        ]))

        def rec = database.getEntity("testtableAdmin").get(4444L)
        assertEquals("test", rec.getValue("name"))
    }
}
