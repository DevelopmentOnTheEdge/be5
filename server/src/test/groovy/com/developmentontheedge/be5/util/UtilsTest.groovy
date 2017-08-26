package com.developmentontheedge.be5.util

import com.developmentontheedge.be5.api.services.SqlService
import com.developmentontheedge.be5.test.AbstractProjectTest
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import org.junit.Test

import static org.junit.Assert.*
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.verify

class UtilsTest extends AbstractProjectTest
{
    SqlService db = sqlMockInjector.get(SqlService.class)

    @Test
    void inClause() throws Exception
    {
        assertEquals "(?, ?, ?, ?, ?)", Utils.inClause(5)

        assertEquals "SELECT code FROM table WHERE id IN (?, ?, ?, ?, ?)",
                "SELECT code FROM table WHERE id IN " + Utils.inClause(5)
    }

    @Test
    void isEmptyTest() throws Exception
    {
        assertTrue Utils.isEmpty(null)
        assertTrue Utils.isEmpty("")
        assertTrue Utils.isEmpty([] as Object[])
        assertTrue Utils.isEmpty([] as List)
        assertTrue Utils.isEmpty([] as List<String>)

        assertFalse Utils.isEmpty(1)
        assertFalse Utils.isEmpty("1")
        assertFalse Utils.isEmpty([1] as Object[])
        assertFalse Utils.isEmpty([1] as List)
        assertFalse Utils.isEmpty(["1"] as List<String>)
    }

    @Test
    void changeType() throws Exception
    {
        assertEquals 3L, Utils.changeType("3", Long)
        assertEquals 3, Utils.changeType("3", Integer)
    }

    @Test
    void changeTypeArray() throws Exception
    {
        String[] stringArray = ["1", "2","3"] as String[]

        assertArrayEquals( [1L, 2L, 3L] as Long[], Utils.changeType(stringArray, Long[]))
        assertArrayEquals( [1, 2, 3] as Integer[], Utils.changeType(stringArray, Integer[]))
    }

    @Test
    void testDbUpdateWithChangeType() throws Exception
    {
        String[] records = ["1", "2"]
        db.update("DELETE users WHERE id IN " + Utils.inClause(records.length),
                Utils.changeType(records, Long[]))

        verify(SqlServiceMock.mock).update(eq("DELETE users WHERE id IN (?, ?)"), eq(1L), eq(2L))
    }


}