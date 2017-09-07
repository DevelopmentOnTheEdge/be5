package com.developmentontheedge.be5.api.services

import com.developmentontheedge.be5.model.FormPresentation
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
                "ErrorProcessing", "", "{'name':'errorInProperty'}")).getFirst()

        assertEquals "{'displayName':'name','status':'error','message':'Error in property'}",
                oneQuotes(first.getBean().getJsonObject("meta").getJsonObject("/name").toString())
    }




}