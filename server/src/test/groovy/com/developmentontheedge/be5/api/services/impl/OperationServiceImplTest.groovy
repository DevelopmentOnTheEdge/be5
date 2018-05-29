package com.developmentontheedge.be5.api.services.impl

import com.developmentontheedge.be5.server.test.TestUtils
import com.developmentontheedge.beans.DynamicPropertySetSupport
import org.junit.Test

import static org.junit.Assert.*


class OperationServiceImplTest extends TestUtils
{
    @Test
    void replaceValuesToStringDigitsAndDate()
    {
        def dps = getDps(new DynamicPropertySetSupport(), [
                test1: "1",
            test2: 1,
            test3: 1L,
            test4: 1.1,
            test5: new java.sql.Date(123123123123)
        ])

        OperationServiceImpl.replaceValuesToString(dps)
        assertEquals([
                test1: "1",
            test2: "1",
            test3: "1",
            test4: "1.1",
            test5: new java.sql.Date(123123123123).toString()
        ], dps.asMap())
    }

    @Test
    void replaceValuesToString()
    {
        def dps = getDps(new DynamicPropertySetSupport(), [
                test6: (["1", "2"] as String[]),
        ])

        OperationServiceImpl.replaceValuesToString(dps)
        assertArrayEquals(["1", "2"] as String[], dps.asMap().get("test6"))


        dps = getDps(new DynamicPropertySetSupport(), [
                test6: ([1, "2"] as Object[]),
        ])

        OperationServiceImpl.replaceValuesToString(dps)
        assertArrayEquals([1, "2"] as Object[], dps.asMap().get("test6"))
    }

}