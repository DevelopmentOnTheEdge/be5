package com.developmentontheedge.be5.api.services

import com.developmentontheedge.be5.api.exceptions.Be5Exception
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

        assertEquals "{'displayName':'name','status':'error','message':'Error in property (getParameters)'}",
                oneQuotes(first.getBean().getJsonObject("meta").getJsonObject("/name").toString())

        assertEquals "{" +
                "'details':'java.lang.IllegalArgumentException: name: name, type:class java.lang.String, value: generateErrorInProperty'," +
                "'message':'name: name, type:class java.lang.String, value: generateErrorInProperty'," +
                "'status':'error'}", oneQuotes(jsonb.toJson(first.getOperationResult()))
    }

    @Test
    void executePropertyError()
    {
        FormPresentation first = operationService.execute(getSpyMockRecForOp("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'executeErrorInProperty'}")).getFirst()

        assertEquals "{'displayName':'name','status':'error','message':'Error in property (invoke)'}",
                oneQuotes(first.getBean().getJsonObject("meta").getJsonObject("/name").toString())

        assertEquals "{" +
                "'details':'java.lang.IllegalArgumentException: name: name, type:class java.lang.String, value: executeErrorInProperty'," +
                "'message':'name: name, type:class java.lang.String, value: executeErrorInProperty'," +
                "'status':'error'}", oneQuotes(jsonb.toJson(first.getOperationResult()))
    }

    @Test
    void generateErrorStatus()
    {
        OperationResult second = operationService.generate(getSpyMockRecForOp("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'generateErrorStatus'}")).getSecond()

        assertEquals "{'details':'message','message':'The operation can not be performed.','status':'error'}",
                oneQuotes(jsonb.toJson(second))
    }

    @Test
    void executeErrorStatus()
    {
        FormPresentation first = operationService.execute(getSpyMockRecForOp("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'executeErrorStatus'}")).getFirst()

        assertEquals "{'displayName':'name'}",
                oneQuotes(first.getBean().getJsonObject("meta").getJsonObject("/name").toString())

        assertEquals "{'details':'message','message':'An error occurred while performing operations.','status':'error'}",
                oneQuotes(jsonb.toJson(first.getOperationResult()))
    }

    @Test(expected = Be5Exception)
    void generateDeveloperError()
    {
        operationService.generate(getSpyMockRecForOp("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'generateDeveloperError'}"))
    }

    @Test(expected = Be5Exception)
    void executeDeveloperError()
    {
        operationService.execute(getSpyMockRecForOp("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'executeDeveloperError'}"))
    }

}