package com.developmentontheedge.be5.util

import com.developmentontheedge.be5.api.services.SqlService
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.test.Be5ProjectTest
import com.developmentontheedge.be5.test.mocks.SqlServiceMock
import org.junit.Test

import java.sql.Time
import java.sql.Timestamp
import java.text.SimpleDateFormat

import static org.junit.Assert.*
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.verify


class UtilsTest extends Be5ProjectTest
{
    @Inject SqlService db

    private SimpleDateFormat dateFormatter = new SimpleDateFormat( "yyyy-MM-dd" )
    private SimpleDateFormat timeFormatter = new SimpleDateFormat( "HH:mm:ss" )
    private SimpleDateFormat dateTimeFormatter = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" )

    @Test
    void inClause()
    {
        assertEquals "(?, ?, ?, ?, ?)", Utils.inClause(5)

        assertEquals "SELECT code FROM table WHERE id IN (?, ?, ?, ?, ?)",
                "SELECT code FROM table WHERE id IN " + Utils.inClause(5)
    }

    @Test
    void addPrefix()
    {
        assertArrayEquals(['companies.1','companies.2'] as String[],
                Utils.addPrefix(['1','2'] as String[], "companies."))
    }

    @Test
    void isEmptyTest()
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
    void changeType()
    {
        assertEquals 3L, Utils.changeType("3", Long)
        assertEquals 3, Utils.changeType("3", Integer)
    }

    @Test
    void changeTypeDateTime()
    {
        assertEquals dateFormatter.parse("2017-08-27"), Utils.changeType( "2017-08-27", java.sql.Date.class)
        assertEquals dateFormatter.parse("2017-08-27"), Utils.changeType( "2017-08-27", Date.class)

        assertEquals timeFormatter.parse("20:49:01"), Utils.changeType( "20:49:01", Time.class)

        assertEquals dateTimeFormatter.parse("2017-08-27 20:49:01"),
                Utils.changeType( "2017-08-27 20:49:01", Timestamp.class)
    }

    @Test
    void changeTypeArray()
    {
        String[] stringArray = ["1", "2","3"] as String[]

        assertArrayEquals( [1L, 2L, 3L] as Long[], Utils.changeType(stringArray, Long[]))
        assertArrayEquals( [1, 2, 3] as Integer[], Utils.changeType(stringArray, Integer[]))
    }

    @Test
    void testDbUpdateWithChangeType()
    {
        String[] records = ["1", "2"]
        db.update("DELETE users WHERE id IN " + Utils.inClause(records.length),
                Utils.changeType(records, Long[]))

        verify(SqlServiceMock.mock).update(eq("DELETE users WHERE id IN (?, ?)"), eq(1L), eq(2L))
    }


}