package com.developmentontheedge.be5.util

import org.junit.Test

import static com.developmentontheedge.be5.FrontendConstants.SEARCH_PARAM
import static com.developmentontheedge.be5.FrontendConstants.SEARCH_PRESETS_PARAM
import static org.junit.Assert.assertEquals

class FilterUtilTest
{
    @Test
    void getOperationParamsTest()
    {
        assertEquals([payable: "no"], FilterUtil.getContextParams([payable: "no"]))
    }

    @Test
    void getOperationParamsTestWithSearchParams()
    {
        def map = [payable: "no", (SEARCH_PARAM): "true"]

        assertEquals([:], FilterUtil.getContextParams(map))
    }

    @Test
    void getOperationParamsTestWithSearchParamsContain()
    {
        def map = [
           payable: "no",
           CODE: "123",
           (SEARCH_PARAM): "true",
           (SEARCH_PRESETS_PARAM): ["payable", "CODE"] as String[]
        ]
        assertEquals([CODE: "123", payable: "no"], FilterUtil.getContextParams(map))
    }

    @Test
    void getFilterParamsTest()
    {
        assertEquals([:], FilterUtil.getFilterParams([payable: "no"]))
    }

    @Test
    void getFilterParamsTestWithSearchParams()
    {
        def map = [payable: "no", (SEARCH_PARAM): "true"]

        assertEquals([payable: "no"], FilterUtil.getFilterParams(map))
    }

    @Test
    void getFilterParamsTestWithSearchParamsContain()
    {
        def map = [payable: "no", CODE: "123", (SEARCH_PARAM): "true", (SEARCH_PRESETS_PARAM): "CODE"]

        assertEquals([payable: "no"], FilterUtil.getFilterParams(map))
    }

    @Test
    void getSearchPresetParam()
    {
        def map = [payable: "no", CODE: "123", (SEARCH_PARAM): "true", (SEARCH_PRESETS_PARAM): "CODE"]
        assertEquals("CODE", FilterUtil.getSearchPresetParam(map))
        assertEquals("test", FilterUtil.getSearchPresetParam(["test": "value"]))
        assertEquals(null, FilterUtil.getSearchPresetParam(["test": "value", (SEARCH_PARAM): "true"]))
    }
}
