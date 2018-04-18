package com.developmentontheedge.be5.operations.operationParams

import com.developmentontheedge.be5.operation.OperationContext
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.test.SqlMockOperationTest
import com.developmentontheedge.be5.util.Either
import org.junit.Test

import static com.developmentontheedge.be5.api.FrontendConstants.SEARCH_PARAM
import static com.developmentontheedge.be5.api.FrontendConstants.SEARCH_PRESETS_PARAM
import static org.junit.Assert.assertEquals


class CustomOpTest extends SqlMockOperationTest
{
    @Test
    void simple()
    {
        def res = getResult([name:"a", value:"1"], [:])

        assertEquals("{name=a, value=1}", oneQuotes(res.getSecond().getMessage()))
    }

    @Test
    void opParams()
    {
        def res = getResult([name:"a", value:"1"], [name:"b"])

        assertEquals("{name=b, value=1}", oneQuotes(res.getSecond().getMessage()))
    }

    @Test
    void filterParam()
    {
        def res = getResult([name:"a", value:"1"], [name:"b", (SEARCH_PARAM): "true"])

        assertEquals("{name=a, value=1}", oneQuotes(res.getSecond().getMessage()))
    }

    @Test
    void filterAndOpParamParam()
    {
        def res = getResult([name:"a", value:"1"], [name:"b", (SEARCH_PARAM): "true", (SEARCH_PRESETS_PARAM): "name"])

        assertEquals("{name=b, value=1}", oneQuotes(res.getSecond().getMessage()))
    }

    private Either<Object, OperationResult> getResult(Map<String, Object> presetValues, Map<String, String> operationParams)
    {
        def operation = createOperation("testtableAdmin", "PrintParamsCustomOp",
                new OperationContext([] as String[], "All records", operationParams))

        return executeOperation(operation, presetValues)
    }
}

