package com.developmentontheedge.be5.operation.operationParams

import com.developmentontheedge.be5.operation.OperationsSqlMockProjectTest
import com.developmentontheedge.be5.operation.model.Operation
import com.developmentontheedge.be5.operation.model.OperationContext
import org.junit.Test

import static com.developmentontheedge.be5.base.FrontendConstants.SEARCH_PARAM
import static com.developmentontheedge.be5.base.FrontendConstants.SEARCH_PRESETS_PARAM
import static org.junit.Assert.assertEquals

class GetRedirectParamsTest extends OperationsSqlMockProjectTest
{
    @Test
    void simple()
    {
        def op = getOperation([:])

        assertEquals("[:]", op.getRedirectParams().toString())
    }

    @Test
    void opParams()
    {
        def op = getOperation([name:"b"])

        assertEquals("[name:b]", op.getRedirectParams().toString())
    }

    @Test
    void filterParam()
    {
        def op = getOperation([name:"b", (SEARCH_PARAM): "true"])

        assertEquals("[name:b, _search_:true]", op.getRedirectParams().toString())
    }

    @Test
    void filterAndOpParamParam()
    {
        def op = getOperation([name:"b", (SEARCH_PARAM): "true", (SEARCH_PRESETS_PARAM): "name"])

        assertEquals("[_search_presets_:name, name:b, _search_:true]", op.getRedirectParams().toString())
    }

    @Test
    void opParamsAndRemoveRedirectParam()
    {
        def op = getOperation([name:"b"])
        op.addRedirectParams([name:""])

        assertEquals("[:]", op.getRedirectParams().toString())
    }

    @Test
    void opParamsAndSetRedirectParam()
    {
        def op = getOperation([name:"b"])

        op.addRedirectParam("name", "a")
        op.addRedirectParams([test:"c"])

        assertEquals("[test:c, name:a]", op.getRedirectParams().toString())
    }

    private Operation getOperation(Map<String, String> operationParams)
    {
        return createOperation("testtableAdmin", "PrintParamsCustomOp",
            new OperationContext([] as String[], "All records", operationParams))
    }
}

