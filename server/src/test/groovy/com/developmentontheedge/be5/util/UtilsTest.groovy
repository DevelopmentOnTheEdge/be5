package com.developmentontheedge.be5.util

import com.developmentontheedge.be5.api.helpers.SqlHelper
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
    SqlHelper sqlHelper = sqlMockInjector.get(SqlHelper.class)

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
        db.update("DELETE users WHERE id IN " + sqlHelper.inClause(records.length),
                Utils.changeType(records, Long[]))

        verify(SqlServiceMock.mock).update(eq("DELETE users WHERE id IN (?, ?)"), eq(1L), eq(2L))
    }


}