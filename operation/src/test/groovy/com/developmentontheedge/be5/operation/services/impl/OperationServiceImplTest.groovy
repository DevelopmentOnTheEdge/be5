package com.developmentontheedge.be5.operation.services.impl

import com.developmentontheedge.be5.operation.OperationsSqlMockProjectTest
import com.developmentontheedge.beans.DynamicProperty
import com.developmentontheedge.beans.DynamicPropertySet
import com.developmentontheedge.beans.DynamicPropertySetSupport
import org.junit.Test

import static org.junit.Assert.assertArrayEquals
import static org.junit.Assert.assertEquals

class OperationServiceImplTest extends OperationsSqlMockProjectTest
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

        new OperationServiceImpl(null, null).replaceValuesToString(dps)
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

        new OperationServiceImpl(null, null).replaceValuesToString(dps)
        assertArrayEquals(["1", "2"] as String[], dps.asMap().get("test6"))


        dps = getDps(new DynamicPropertySetSupport(), [
                test6: ([1, "2"] as Object[]),
        ])

        new OperationServiceImpl(null, null).replaceValuesToString(dps)
        assertArrayEquals([1, "2"] as Object[], dps.asMap().get("test6"))
    }

    @Test
    void replaceValuesToStringDPSDigitAndDate() {
        def dps = getDps(new DynamicPropertySetSupport(), [
                test1: getDps(new DynamicPropertySetSupport(),
                        [
                                test2: "1",
                                test3: 1,
                                test4: 1L,
                                test5: 1.1,
                                test6: new java.sql.Date(123123123123)
                        ]),
        ])

//        new OperationServiceImpl(null, null).replaceValuesToString(dps)
//        assertEquals([test1:getDps(new DynamicPropertySetSupport(),[dps]),], dps.asMap());

        new OperationServiceImpl(null, null).replaceValuesToString(dps)
        assertEquals([
                test2: "1",
                test3: "1",
                test4: "1",
                test5: "1.1",
                test6: new java.sql.Date(123123123123).toString()
        ], ((DynamicPropertySet)dps.getValue("test1")).asMap())


//        dps = getDps(new DynamicPropertySetSupport(), [
//                test6: ([1, "2"] as Object[]),
//        ])
//
//        new OperationServiceImpl(null, null).replaceValuesToString(dps)
//        assertArrayEquals([1, "2"] as Object[], dps.asMap().get("test7"))
    }

    static <T extends DynamicPropertySet> T getDps(T dps, Map<String, ?> nameValues)
    {
        for (Map.Entry<String, ?> entry : nameValues.entrySet())
        {
            dps.add(new DynamicProperty(entry.getKey(), entry.getValue().getClass(), entry.getValue()));
        }
        return dps;
    }
}
