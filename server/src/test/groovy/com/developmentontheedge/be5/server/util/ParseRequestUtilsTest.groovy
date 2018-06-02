package com.developmentontheedge.be5.server.util

import com.developmentontheedge.be5.operation.util.FilterUtil
import org.junit.Test

import static com.developmentontheedge.be5.base.FrontendConstants.SEARCH_PARAM
import static com.developmentontheedge.be5.base.FrontendConstants.SEARCH_PRESETS_PARAM
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
        assertEquals([payable:"no"], FilterUtil.getOperationParamsWithoutFilter([payable:"no"]))
    }

    @Test
    void setOperationParamsTestWithSearchParams()
    {
        def map = [payable:"no", (SEARCH_PARAM):"true"]

        assertEquals([:], FilterUtil.getOperationParamsWithoutFilter(map))
    }

    @Test
    void setOperationParamsTestWithSearchParamsContain()
    {
        def map = [payable:"no", CODE:"123", (SEARCH_PARAM): "true", (SEARCH_PRESETS_PARAM): "payable,CODE"]

        assertEquals([CODE:"123", payable:"no"], FilterUtil.getOperationParamsWithoutFilter(map))
    }

}