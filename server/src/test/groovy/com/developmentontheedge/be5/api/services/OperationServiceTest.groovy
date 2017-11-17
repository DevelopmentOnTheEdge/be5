package com.developmentontheedge.be5.api.services

import com.developmentontheedge.be5.api.exceptions.Be5Exception
import com.developmentontheedge.be5.model.FormPresentation
import com.developmentontheedge.be5.operation.Operation
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
        Object first = generateOperation(operation, "{'name':'generateErrorInProperty'}").getFirst()

        assertEquals "{'displayName':'name','status':'error','message':'Error in property (getParameters)'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("meta").getJsonObject("/name").toString())

        assertEquals "{'details':'java.lang.IllegalArgumentException: " +
                "[ name: 'name', type: class java.lang.String, value: generateErrorInProperty (String) ]'," +
                "'message':'[ name: 'name', type: class java.lang.String, value: generateErrorInProperty (String) ]'," +
                "'status':'error'}",
                oneQuotes(jsonb.toJson(operation.getResult()))
    }

    @Test
    void executePropertyError()
    {
        def operation = getOperation("testtableAdmin", "All records", "ErrorProcessing", "")
        Object first = executeOperation(operation, "{'name':'executeErrorInProperty'}").getFirst()

        assertEquals "{'displayName':'name','status':'error','message':'Error in property (invoke)'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("meta").getJsonObject("/name").toString())

        assertEquals "{'details':'java.lang.IllegalArgumentException: " +
                "[ name: 'name', type: class java.lang.String, value: executeErrorInProperty (String) ]'," +
                "'message':'[ name: 'name', type: class java.lang.String, value: executeErrorInProperty (String) ]'," +
                "'status':'error'}",
                oneQuotes(jsonb.toJson(operation.getResult()))
    }

    @Test
    void generateErrorStatus()
    {
        OperationResult second = generateOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'generateErrorStatus'}").getSecond()

        assertEquals "{'message':'The operation can not be performed.','status':'error'}",
                oneQuotes(jsonb.toJson(second))
    }

    @Test
    void generateErrorStatusOnExecute()
    {
        OperationResult second = executeOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'generateErrorStatus'}").getSecond()

        assertEquals "{'message':'The operation can not be performed.','status':'error'}",
                oneQuotes(jsonb.toJson(second))
    }

    @Test
    void executeErrorStatus()
    {
        def operation = getOperation("testtableAdmin", "All records", "ErrorProcessing", "")
        Object first = executeOperation(operation, "{'name':'executeErrorStatus'}").getFirst()

        assertEquals "{'displayName':'name'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("meta").getJsonObject("/name").toString())

        assertEquals "{'message':'An error occurred while performing operations.','status':'error'}",
                oneQuotes(jsonb.toJson(operation.getResult()))
    }

    @Test(expected = Be5Exception)
    void generateDeveloperError()
    {
        generateOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'generateDeveloperError'}")
    }

    @Test(expected = Be5Exception)
    void executeDeveloperError()
    {
        executeOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'executeDeveloperError'}")
    }

    @Test
    void errorHandlingCycles()
    {
        Object first = generateOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'generateErrorInProperty'}").getFirst()

        assertEquals "{'name':'generateErrorInProperty','propertyForAnotherEntity':'text'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString())


        //call callGetParameters and add OperationResult from invoke to FormPresentation
        first = executeOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'executeErrorStatus'}").getFirst()

        assertEquals "{'name':'executeErrorStatus','propertyForAnotherEntity':'text'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString())


        //just return FormPresentation with current parameters
        first = executeOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", "{'name':'executeErrorInProperty'}").getFirst()

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