package com.developmentontheedge.be5.util

import org.junit.Test

import static com.developmentontheedge.be5.components.FrontendConstants.SEARCH_PARAM
import static com.developmentontheedge.be5.components.FrontendConstants.SEARCH_PRESETS_PARAM
import static org.junit.Assert.*


class ParseRequestUtilsTest
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
        assertEquals([payable:"no"], ParseRequestUtils.getOperationParams([payable:"no"]))
    }

    @Test
    void setOperationParamsTestWithSearchParams()
    {
        def map = [payable:"no"]
        map.put(SEARCH_PARAM, "true")

        assertEquals([:], ParseRequestUtils.getOperationParams(map))
    }

    @Test
    void setOperationParamsTestWithSearchParamsContain()
    {
        def map = [payable:"no", CODE:"123"]
        map.put(SEARCH_PARAM, "true")
        map.put(SEARCH_PRESETS_PARAM, "payable,CODE")

        assertEquals([CODE:"123", payable:"no"], ParseRequestUtils.getOperationParams(map))
    }
}