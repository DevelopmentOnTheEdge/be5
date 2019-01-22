package com.developmentontheedge.be5.operation.services

import com.developmentontheedge.be5.exceptions.Be5ErrorCode
import com.developmentontheedge.be5.exceptions.Be5Exception
import com.developmentontheedge.be5.metadata.RoleType
import com.developmentontheedge.be5.operation.OperationsSqlMockProjectTest
import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.operation.OperationStatus
import com.developmentontheedge.beans.json.JsonFactory
import org.junit.Ignore
import org.junit.Test

import static org.junit.Assert.assertEquals

class OperationServiceTest extends OperationsSqlMockProjectTest
{
    @Test
    void generate()
    {
        def operation = createOperation("testtableAdmin", "All records", "ErrorProcessing", "")
        Object first = generateOperation(operation).getFirst()

        assertEquals "{'displayName':'name','columnSize':'30'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("meta").getJsonObject("/name").toString())

        assertEquals OperationStatus.GENERATE, operation.getStatus()
    }

    private Operation propertyError(Map<String, String> presetValues)
    {
        def operation = createOperation("testtableAdmin", "All records", "ErrorProcessing", "")
        Object first = generateOperation(operation, presetValues).getFirst()

        assertEquals "{'displayName':'name','columnSize':'30','status':'error','message':'Error in property (getParameters)'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("meta").getJsonObject("/name").toString())
        return operation
    }

    /* generateErrorInProperty */

    @Test
    void generatePropertyError()
    {
        def operation = propertyError(['name': 'generateErrorInProperty'])

        assertEquals OperationStatus.GENERATE, operation.getResult().getStatus()
    }

    @Test
    void generatePropertyErrorReload()
    {
        setStaticUserInfo(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER)

        def operation = propertyError(['_reloadControl_': '/name', 'name': 'generateErrorInProperty'])

        assertEquals OperationStatus.GENERATE, operation.getResult().getStatus()
//        assertEquals "Error in property (getParameters)",// - [ name: 'name', type: class java.lang.String, value: generateErrorInProperty (String) ]",
//                operation.getResult().getMessage()
    }

    @Test
    void generatePropertyErrorNotSysDev()
    {
        setStaticUserInfo(RoleType.ROLE_ADMINISTRATOR)

        def operation = propertyError(['name': 'generateErrorInProperty'])

        assertEquals OperationStatus.GENERATE, operation.getResult().getStatus()
    }

    @Test
    void generatePropertyErrorNotSysDevReload()
    {
        setStaticUserInfo(RoleType.ROLE_ADMINISTRATOR)

        def operation = propertyError(['_reloadControl_': '/name', 'name': 'generateErrorInProperty'])

        assertEquals OperationStatus.GENERATE, operation.getResult().getStatus()
    }

    @Test
    void executeWithGenerateErrorInProperty()
    {
        def operation = createOperation("testtableAdmin", "All records", "ErrorProcessing", "")
        Object first = executeOperation(operation, ['name': 'generateErrorInProperty']).getFirst()

        assertEquals "{'displayName':'name','columnSize':'30','status':'error','message':'Error in property (getParameters)'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("meta").getJsonObject("/name").toString())

        assertEquals OperationStatus.ERROR, operation.getResult().getStatus()
        assertEquals "Error in property (getParameters)",// - [ name: 'name', type: class java.lang.String, value: generateErrorInProperty (String) ]",
                operation.getResult().getMessage()
    }

    /* executeErrorInProperty */

    @Test
    void executePropertyError()
    {
        def operation = createOperation("testtableAdmin", "All records", "ErrorProcessing", "")
        Object first = executeOperation(operation, ['name': 'executeErrorInProperty']).getFirst()

        assertEquals "{'displayName':'name','columnSize':'30','status':'error','message':'Error in property (invoke)'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("meta").getJsonObject("/name").toString())

        assertEquals OperationStatus.ERROR, operation.getResult().getStatus()
        assertEquals "Error in property (invoke)",// - [ name: 'name', type: class java.lang.String, value: executeErrorInProperty (String) ]",
                operation.getResult().getMessage()
    }

    /* generateErrorStatus */

    /**
     * выдать ошибку, не переходя на форму
     * (нужно доделать фронтенд чтобы небыло перехода на операцию при возвращении Either.second)
     */
    @Test
    void generateErrorStatus()
    {
        OperationResult second = generateOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", ['name': 'generateErrorStatus']).getSecond()

        assertEquals OperationStatus.ERROR, second.getStatus()
        assertEquals "The operation can not be performed.", second.getMessage()
    }

    @Test
    void generateErrorStatusOnExecute()
    {
        def either = executeOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", ['name': 'generateErrorStatus'])
        OperationResult second = either.getSecond()

        assertEquals OperationStatus.ERROR, second.getStatus()
        assertEquals "The operation can not be performed.", second.getMessage()
    }

    @Test
    void executeErrorStatus()
    {
        def operation = createOperation("testtableAdmin", "All records", "ErrorProcessing", "")
        Object first = executeOperation(operation, ['name': 'executeErrorStatus']).getFirst()

        assertEquals "{'displayName':'name','columnSize':'30'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("meta").getJsonObject("/name").toString())

        assertEquals OperationStatus.ERROR, operation.getResult().getStatus()
        assertEquals "An error occurred while performing operations.", operation.getResult().getMessage()
    }

    @Test(expected = Be5Exception)
    void generateError()
    {
        def operation = createOperation("testtableAdmin", "All records", "ErrorProcessing", "")
        generateOperation(operation, ['name': 'generateError'])
    }

    @Test
    void executeError()
    {
        def operation = createOperation("testtableAdmin", "All records", "ErrorProcessing", "")
        executeOperation(operation, ['name': 'executeError'])

        assertEquals(OperationStatus.ERROR, operation.getStatus())
        assertEquals(Be5ErrorCode.INTERNAL_ERROR_IN_OPERATION, ((Be5Exception) operation.getResult().getDetails()).code)
    }

    @Test
    void errorHandlingCycles()
    {
        Object first = generateOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", ['name': 'generateErrorInProperty']).getFirst()

        assertEquals "{'name':'generateErrorInProperty','propertyForAnotherEntity':'text'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString())

        //call callGetParameters and add OperationResult from invoke to FormPresentation
        first = executeOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", ['name': 'executeErrorStatus']).getFirst()

        assertEquals "{'name':'executeErrorStatus','propertyForAnotherEntity':'text'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString())

        //just return FormPresentation with current parameters
        first = executeOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", ['name': 'executeErrorInProperty']).getFirst()

        assertEquals "{'name':'executeErrorInProperty'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString())
    }

    @Test
    void generateCallTest()
    {
        def operation = createOperation("testtableAdmin", "All records", "ErrorProcessing", "")
        generateOperation(operation, ['name': 'generateCall']).getFirst()
    }

    @Test
    void executeOperationWithoutParams()
    {
        OperationResult second = executeOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", ['name': 'withoutParams']).getSecond()

        assertEquals OperationStatus.ERROR, second.getStatus()
        assertEquals "Error in operation", second.getMessage()
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
                "ErrorProcessing", "", ['name': 'generateErrorInProperty', 'booleanProperty': 'false']).getFirst()

        assertEquals "{'name':'generateErrorInProperty','propertyForAnotherEntity':'text','booleanProperty':'false'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString())

        //call callGetParameters and add OperationResult from invoke to FormPresentation
        first = executeOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", ['name': 'executeErrorStatus', 'booleanProperty': 'false']).getFirst()

        assertEquals "{'name':'executeErrorStatus','propertyForAnotherEntity':'text','booleanProperty':false}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString())

        //just return FormPresentation with current parameters
        first = executeOperation("testtableAdmin", "All records",
                "ErrorProcessing", "", ['name': 'executeErrorInProperty', 'booleanProperty': 'false']).getFirst()

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
                "ErrorProcessing", "", ['name': 'generateErrorInProperty', 'booleanProperty': false]).getFirst()

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
                "ErrorProcessing", "", ['name': 'executeErrorInProperty', 'booleanProperty': false]).getFirst()

        assertEquals "{'name':'executeErrorInProperty','booleanProperty':false}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString())
    }
}
