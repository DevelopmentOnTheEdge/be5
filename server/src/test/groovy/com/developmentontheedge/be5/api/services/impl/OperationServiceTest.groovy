package com.developmentontheedge.be5.api.services.impl

import com.developmentontheedge.be5.api.exceptions.Be5Exception
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.operation.OperationStatus
import com.developmentontheedge.be5.test.SqlMockOperationTest
import com.developmentontheedge.beans.json.JsonFactory
import org.junit.Ignore
import org.junit.Test

import static org.junit.Assert.*


class OperationServiceTest extends SqlMockOperationTest
{
    @Test
    void generate()
    {
        def operation = getOperation("testtableAdmin", "All records", "ErrorProcessing", "")
        Object first = generateOperation(operation).getFirst()

        assertEquals "{'displayName':'name'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("meta").getJsonObject("/name").toString())

        assertEquals OperationStatus.OPEN, operation.getStatus()
    }

    @Test
    void generatePropertyError()
    {
        def operation = getOperation("testtableAdmin", "All records", "ErrorProcessing", "")
        Object first = generateOperation(operation, ['name':'generateErrorInProperty']).getFirst()

        assertEquals "{'displayName':'name','status':'error','message':'Error in property (getParameters)'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("meta").getJsonObject("/name").toString())

        assertEquals OperationStatus.ERROR, operation.getResult().getStatus()
        assertEquals "[ name: 'name', type: class java.lang.String, value: generateErrorInProperty (String) ]",
                operation.getResult().getMessage()
    }

    @Test
    void generatePropertyErrorOnExecute()
    {
        def operation = getOperation("testtableAdmin", "All records", "ErrorProcessing", "")
        Object first = executeOperation(operation, ['name':'generateErrorInProperty']).getFirst()

        assertEquals "{'displayName':'name','status':'error','message':'Error in property (getParameters)'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("meta").getJsonObject("/name").toString())

        assertEquals OperationStatus.ERROR, operation.getResult().getStatus()
        assertEquals "[ name: 'name', type: class java.lang.String, value: generateErrorInProperty (String) ]",
                operation.getResult().getMessage()
    }

    @Test
    void executePropertyError()
    {
        def operation = getOperation("testtableAdmin", "All records", "ErrorProcessing", "")
        Object first = executeOperation(operation, ['name':'executeErrorInProperty']).getFirst()

        assertEquals "{'displayName':'name','status':'error','message':'Error in property (invoke)'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("meta").getJsonObject("/name").toString())

        assertEquals OperationStatus.ERROR, operation.getResult().getStatus()
        assertEquals "[ name: 'name', type: class java.lang.String, value: executeErrorInProperty (String) ]",
                operation.getResult().getMessage()
    }

    @Test
    void generateErrorStatus()
    {
        OperationResult second = generateOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", ['name':'generateErrorStatus']).getSecond()

        assertEquals OperationStatus.ERROR, second.getStatus()
        assertEquals "The operation can not be performed.", second.getMessage()
    }

    @Test
    void generateErrorStatusOnExecute()
    {
        def either = executeOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", ['name':'generateErrorStatus'])
        OperationResult second = either.getSecond()

        assertEquals OperationStatus.ERROR, second.getStatus()
        assertEquals "The operation can not be performed.", second.getMessage()
    }

    @Test
    void executeErrorStatus()
    {
        def operation = getOperation("testtableAdmin", "All records", "ErrorProcessing", "")
        Object first = executeOperation(operation, ['name':'executeErrorStatus']).getFirst()

        assertEquals "{'displayName':'name'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("meta").getJsonObject("/name").toString())

        assertEquals OperationStatus.ERROR, operation.getResult().getStatus()
        assertEquals "An error occurred while performing operations.", operation.getResult().getMessage()
    }

    @Test(expected = Be5Exception)
    void generateError()
    {
        def operation = getOperation("testtableAdmin", "All records", "ErrorProcessing", "")
        generateOperation(operation, ['name':'generateError'])

        assertEquals(OperationStatus.ERROR, operation.getStatus())
    }

    @Test//(expected = Be5Exception)
    void executeError()
    {
        def operation = getOperation("testtableAdmin", "All records", "ErrorProcessing", "")
        executeOperation(operation, ['name':'executeError'])

        assertEquals(OperationStatus.ERROR, operation.getStatus())
    }

    @Test
    void errorHandlingCycles()
    {
        Object first = generateOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", ['name':'generateErrorInProperty']).getFirst()

        assertEquals "{'name':'generateErrorInProperty','propertyForAnotherEntity':'text'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString())


        //call callGetParameters and add OperationResult from invoke to FormPresentation
        first = executeOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", ['name':'executeErrorStatus']).getFirst()

        assertEquals "{'name':'executeErrorStatus','propertyForAnotherEntity':'text'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString())


        //just return FormPresentation with current parameters
        first = executeOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", ['name':'executeErrorInProperty']).getFirst()

        assertEquals "{'name':'executeErrorInProperty'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString())
    }

    /**
     * возможно на фронтенд лучше всегда отправлять прошедшие валидатор значения для boolean и чисел, вместо строк.
     * Меньше логики на фронтенде.
     */
    @Test
    @Ignore
    void errorHandlingCyclesCastTypesString()
    {
        Object first = generateOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'generateErrorInProperty','booleanProperty':'false'}").getFirst()

        assertEquals "{'name':'generateErrorInProperty','propertyForAnotherEntity':'text','booleanProperty':'false'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString())


        //call callGetParameters and add OperationResult from invoke to FormPresentation
        first = executeOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'executeErrorStatus','booleanProperty':'false'}").getFirst()

        assertEquals "{'name':'executeErrorStatus','propertyForAnotherEntity':'text','booleanProperty':false}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString())


        //just return FormPresentation with current parameters
        first = executeOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'executeErrorInProperty','booleanProperty':'false'}").getFirst()

        assertEquals "{'name':'executeErrorInProperty','booleanProperty':false}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString())
    }

    /**
     * возможно на фронтенд лучше всегда отправлять прошедшие валидатор значения для boolean и чисел, вместо строк.
     * Меньше логики на фронтенде.
     */
    @Test
    @Ignore
    void errorHandlingCyclesCastTypesBoolean()
    {
        Object first = generateOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'generateErrorInProperty','booleanProperty':false}").getFirst()

        assertEquals "{'name':'generateErrorInProperty','propertyForAnotherEntity':'text','booleanProperty':'false'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString())


//        //call callGetParameters and add OperationResult from invoke to FormPresentation
//        first = executeOperation("testtableAdmin", "All records",
//                "ErrorProcessing", "", "{'name':'executeErrorStatus','booleanProperty':false}")).getFirst()
//
//        assertEquals "{'name':'executeErrorStatus','propertyForAnotherEntity':'text','booleanProperty':false}",
//                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString())


        //just return FormPresentation with current parameters
        first = executeOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'executeErrorInProperty','booleanProperty':false}").getFirst()

        assertEquals "{'name':'executeErrorInProperty','booleanProperty':false}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString())
    }
}