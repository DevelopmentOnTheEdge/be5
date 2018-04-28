package com.developmentontheedge.be5.util

import com.developmentontheedge.be5.test.TestUtils
import com.developmentontheedge.beans.DynamicPropertySetSupport
import org.junit.Test

import static com.developmentontheedge.be5.api.FrontendConstants.SEARCH_PARAM
import static com.developmentontheedge.be5.api.FrontendConstants.SEARCH_PRESETS_PARAM
import static org.junit.Assert.*


class ParseRequestUtilsTest extends TestUtils
{
    @Test
    void selectedRowsTest()
    {
        assertTrue( Arrays.equals(["1","2","3"] as String[], ParseRequestUtils.selectedRows("1,2,3")))

        assertTrue( Arrays.equals(["1"] as String[], ParseRequestUtils.selectedRows("1")))

        assertTrue( Arrays.equals([] as String[], ParseRequestUtils.selectedRows("")))
    }

    @Test
    void setOperationParamsTest()
    {
        assertEquals([payable:"no"], ParseRequestUtils.getOperationParamsWithoutFilter([payable:"no"]))
    }

    @Test
    void setOperationParamsTestWithSearchParams()
    {
        def map = [payable:"no", (SEARCH_PARAM):"true"]

        assertEquals([:], ParseRequestUtils.getOperationParamsWithoutFilter(map))
    }

    @Test
    void setOperationParamsTestWithSearchParamsContain()
    {
        def map = [payable:"no", CODE:"123", (SEARCH_PARAM): "true", (SEARCH_PRESETS_PARAM): "payable,CODE"]

        assertEquals([CODE:"123", payable:"no"], ParseRequestUtils.getOperationParamsWithoutFilter(map))
    }

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

        ParseRequestUtils.replaceValuesToString(dps)
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

        ParseRequestUtils.replaceValuesToString(dps)
        assertArrayEquals(["1", "2"] as String[], dps.asMap().get("test6"))


        dps = getDps(new DynamicPropertySetSupport(), [
                test6: ([1, "2"] as Object[]),
        ])

        ParseRequestUtils.replaceValuesToString(dps)
        assertArrayEquals([1, "2"] as Object[], dps.asMap().get("test6"))
    }
}