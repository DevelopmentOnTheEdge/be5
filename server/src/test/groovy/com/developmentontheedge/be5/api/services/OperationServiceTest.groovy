package com.developmentontheedge.be5.api.services

import com.developmentontheedge.be5.model.FormPresentation
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.test.SqlMockOperationTest
import org.junit.Test

import static org.junit.Assert.*

class OperationServiceTest extends SqlMockOperationTest
{
    @Test
    void generate()
    {
        FormPresentation first = operationService.generate(getSpyMockRecForOp("testtableAdmin", "All records",
                "ErrorProcessing", "", "")).getFirst()
        assertEquals "{'displayName':'name'}",
                oneQuotes(first.getBean().getJsonObject("meta").getJsonObject("/name").toString())
    }

    @Test
    void generatePropertyError()
    {
        FormPresentation first = operationService.generate(getSpyMockRecForOp("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'generateErrorInProperty'}")).getFirst()

        assertEquals "{'displayName':'name','status':'error','message':'Error in property (generate)'}",
                oneQuotes(first.getBean().getJsonObject("meta").getJsonObject("/name").toString())
    }

    @Test
    void generateErrorStatus()
    {
        OperationResult second = operationService.generate(getSpyMockRecForOp("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'generateErrorStatus'}")).getSecond()

        assertEquals "{'message':'The operation can not be performed.','status':'error'}",
                oneQuotes(jsonb.toJson(second))
    }

    @Test
    void executeErrorStatus()
    {
        FormPresentation first = operationService.execute(getSpyMockRecForOp("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'executeErrorStatus'}")).getFirst()

        assertEquals "An error occurred while performing operations.",
                oneQuotes(first.getErrorMsg().toString())
    }

}