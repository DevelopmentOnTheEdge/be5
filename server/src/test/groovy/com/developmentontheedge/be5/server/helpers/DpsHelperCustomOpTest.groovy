package com.developmentontheedge.be5.server.helpers

import com.developmentontheedge.be5.base.model.GDynamicPropertySetSupport
import com.developmentontheedge.be5.operation.model.OperationContext
import com.developmentontheedge.be5.operation.model.OperationResult
import com.developmentontheedge.be5.operation.util.Either
import com.developmentontheedge.be5.test.ServerBe5ProjectTest
import org.junit.Before
import org.junit.Test

import javax.inject.Inject

import static com.developmentontheedge.be5.base.FrontendConstants.SEARCH_PARAM
import static com.developmentontheedge.be5.base.FrontendConstants.SEARCH_PRESETS_PARAM
import static org.junit.Assert.assertEquals

class DpsHelperCustomOpTest extends ServerBe5ProjectTest
{
    @Inject DpsHelper dpsHelper

    @Before
    void setUp()
    {
        initGuest()
    }

    @Test
    void simple()
    {
        def res = getResult([name:"a", value:"1"], [:])

        assertEquals("{name=a, value=1}", oneQuotes(res))
    }

    @Test
    void opParams()
    {
        def res = getResult([name:"a", value:"1"], [name:"b"])

        assertEquals("{name=b, value=1}", oneQuotes(res))
    }

    @Test
    void filterParam()
    {
        def res = getResult([name:"a", value:"1"], [name:"b", (SEARCH_PARAM): "true"])

        assertEquals("{name=a, value=1}", oneQuotes(res))
    }

    @Test
    void filterAndOpParamParam()
    {
        def res = getResult([name:"a", value:"1"], [name:"b", (SEARCH_PARAM): "true", (SEARCH_PRESETS_PARAM): "name"])

        assertEquals("{name=b, value=1}", oneQuotes(res))
    }

    private String getResult(Map<String, Object> presetValues, Map<String, String> operationParams)
    {
//        def operation = createOperation("testtableAdmin", "PrintParamsCustomOp",
//                new OperationContext([] as String[], "All records", operationParams))
//
//        return executeOperation(operation, presetValues)
        def dps = new GDynamicPropertySetSupport()

        dpsHelper.addDpExcludeAutoIncrement(dps, meta.getEntity("testtableAdmin"), operationParams, presetValues)

        return dps.asMap().toString()
    }
}
