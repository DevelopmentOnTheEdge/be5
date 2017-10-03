package com.developmentontheedge.be5.api.services

import com.developmentontheedge.be5.api.exceptions.Be5Exception
import com.developmentontheedge.be5.model.FormPresentation
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.test.SqlMockOperationTest
import org.junit.Ignore
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

        assertEquals "{'status':'open'}", oneQuotes(jsonb.toJson(first.getOperationResult()))
    }

    @Test
    void generatePropertyError()
    {
        FormPresentation first = operationService.generate(getSpyMockRecForOp("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'generateErrorInProperty'}")).getFirst()

        assertEquals "{'displayName':'name','status':'error','message':'Error in property (getParameters)'}",
                oneQuotes(first.getBean().getJsonObject("meta").getJsonObject("/name").toString())

        assertEquals "{'details':'java.lang.IllegalArgumentException: " +
                "[ name: 'name', type: class java.lang.String, value: generateErrorInProperty (String) ]'," +
                "'message':'[ name: 'name', type: class java.lang.String, value: generateErrorInProperty (String) ]'," +
                "'status':'error'}",
                oneQuotes(jsonb.toJson(first.getOperationResult()))
    }

    @Test
    void executePropertyError()
    {
        FormPresentation first = operationService.execute(getSpyMockRecForOp("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'executeErrorInProperty'}")).getFirst()

        assertEquals "{'displayName':'name','status':'error','message':'Error in property (invoke)'}",
                oneQuotes(first.getBean().getJsonObject("meta").getJsonObject("/name").toString())

        assertEquals "{'details':'java.lang.IllegalArgumentException: " +
                "[ name: 'name', type: class java.lang.String, value: executeErrorInProperty (String) ]'," +
                "'message':'[ name: 'name', type: class java.lang.String, value: executeErrorInProperty (String) ]'," +
                "'status':'error'}",
                oneQuotes(jsonb.toJson(first.getOperationResult()))
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
    void generateErrorStatusOnExecute()
    {
        OperationResult second = operationService.execute(getSpyMockRecForOp("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'generateErrorStatus'}")).getSecond()

        assertEquals "{'message':'The operation can not be performed.','status':'error'}",
                oneQuotes(jsonb.toJson(second))
    }

    @Test
    void executeErrorStatus()
    {
        FormPresentation first = operationService.execute(getSpyMockRecForOp("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'executeErrorStatus'}")).getFirst()

        assertEquals "{'displayName':'name'}",
                oneQuotes(first.getBean().getJsonObject("meta").getJsonObject("/name").toString())

        assertEquals "{'message':'An error occurred while performing operations.','status':'error'}",
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

    @Test
    void errorHandlingCycles()
    {
        FormPresentation first = operationService.generate(getSpyMockRecForOp("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'generateErrorInProperty'}")).getFirst()

        assertEquals "{'name':'generateErrorInProperty','propertyForAnotherEntity':'text'}",
                oneQuotes(first.getBean().getJsonObject("values").toString())


        //call callGetParameters and add OperationResult from invoke to FormPresentation
        first = operationService.execute(getSpyMockRecForOp("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'executeErrorStatus'}")).getFirst()

        assertEquals "{'name':'executeErrorStatus','propertyForAnotherEntity':'text'}",
                oneQuotes(first.getBean().getJsonObject("values").toString())


        //just return FormPresentation with current parameters
        first = operationService.execute(getSpyMockRecForOp("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'executeErrorInProperty'}")).getFirst()

        assertEquals "{'name':'executeErrorInProperty'}",
                oneQuotes(first.getBean().getJsonObject("values").toString())
    }

    /**
     * возможно на фронтенд лучше всегда отправлять прошедшие валидатор значения для boolean и чисел, вместо строк.
     * Меньше логики на фронтенде.
     */
    @Test
    @Ignore
    void errorHandlingCyclesCastTypesString()
    {
        FormPresentation first = operationService.generate(getSpyMockRecForOp("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'generateErrorInProperty','booleanProperty':'false'}")).getFirst()

        assertEquals "{'name':'generateErrorInProperty','propertyForAnotherEntity':'text','booleanProperty':'false'}",
                oneQuotes(first.getBean().getJsonObject("values").toString())


        //call callGetParameters and add OperationResult from invoke to FormPresentation
        first = operationService.execute(getSpyMockRecForOp("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'executeErrorStatus','booleanProperty':'false'}")).getFirst()

        assertEquals "{'name':'executeErrorStatus','propertyForAnotherEntity':'text','booleanProperty':false}",
                oneQuotes(first.getBean().getJsonObject("values").toString())


        //just return FormPresentation with current parameters
        first = operationService.execute(getSpyMockRecForOp("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'executeErrorInProperty','booleanProperty':'false'}")).getFirst()

        assertEquals "{'name':'executeErrorInProperty','booleanProperty':false}",
                oneQuotes(first.getBean().getJsonObject("values").toString())
    }

    /**
     * возможно на фронтенд лучше всегда отправлять прошедшие валидатор значения для boolean и чисел, вместо строк.
     * Меньше логики на фронтенде.
     */
    @Test
    @Ignore
    void errorHandlingCyclesCastTypesBoolean()
    {
        FormPresentation first = operationService.generate(getSpyMockRecForOp("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'generateErrorInProperty','booleanProperty':false}")).getFirst()

        assertEquals "{'name':'generateErrorInProperty','propertyForAnotherEntity':'text','booleanProperty':'false'}",
                oneQuotes(first.getBean().getJsonObject("values").toString())


//        //call callGetParameters and add OperationResult from invoke to FormPresentation
//        first = operationService.execute(getSpyMockRecForOp("testtableAdmin", "All records",
//                "ErrorProcessing", "", "{'name':'executeErrorStatus','booleanProperty':false}")).getFirst()
//
//        assertEquals "{'name':'executeErrorStatus','propertyForAnotherEntity':'text','booleanProperty':false}",
//                oneQuotes(first.getBean().getJsonObject("values").toString())


        //just return FormPresentation with current parameters
        first = operationService.execute(getSpyMockRecForOp("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'executeErrorInProperty','booleanProperty':false}")).getFirst()

        assertEquals "{'name':'executeErrorInProperty','booleanProperty':false}",
                oneQuotes(first.getBean().getJsonObject("values").toString())
    }
}