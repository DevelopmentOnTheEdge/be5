package com.developmentontheedge.be5.databasemodel.impl

import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.api.services.OperationExecutor
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.operation.OperationStatus
import com.developmentontheedge.be5.test.Be5ProjectTest
import groovy.transform.TypeChecked
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

import static org.junit.Assert.*


@TypeChecked
class OperationModelBaseTest extends Be5ProjectTest
{
    @Inject OperationExecutor operationExecutor
    @Inject Meta meta

    @Rule
    public ExpectedException expectedEx = ExpectedException.none()

    OperationModelBase operationModelBase

    @Before
    void init()
    {
        operationModelBase = new OperationModelBase(meta, operationExecutor)
        operationModelBase.setEntityName("testtableAdmin")
        operationModelBase.setQueryName("All records")
    }

    @Test
    void execute()
    {
        def operation = operationModelBase.execute {
            presetValues  = [ 'name': 'ok' ]
            operationName = "ErrorProcessing"
        }

        assertEquals(OperationStatus.FINISHED, operation.getStatus())
    }

    @Test
    void generateErrorInPropertyOnExecute()
    {
        expectedEx.expect(IllegalArgumentException.class)
        expectedEx.expectMessage("[ name: 'name', type: class java.lang.String, value: generateErrorInProperty (String) ]")
        executeAndCheck('generateErrorInProperty')
    }

    @Test
    void generateErrorStatusOnExecute()
    {
        expectedEx.expect(RuntimeException.class)
        expectedEx.expectMessage("The operation can not be performed.")
        executeAndCheck('generateErrorStatus')
    }

    @Test
    void generateErrorOnExecute()
    {
        expectedEx.expect(RuntimeException.class)
        expectedEx.expectMessage("Internal error occured during operation testtableAdmin.ErrorProcessing")
        executeAndCheck('generateError')
    }

    @Test
    void executeErrorInProperty()
    {
        expectedEx.expect(RuntimeException.class)
        expectedEx.expectMessage("[ name: 'name', type: class java.lang.String, value: executeErrorInProperty (String) ]")
        executeAndCheck('executeErrorInProperty')
    }

    @Test
    void executeErrorStatus()
    {
        expectedEx.expect(RuntimeException.class)
        expectedEx.expectMessage("An error occurred while performing operations.")
        executeAndCheck('executeErrorStatus')
    }

    @Test
    void executeError()
    {
        expectedEx.expect(IllegalArgumentException.class)
        //expectedEx.expectMessage()
        executeAndCheck('executeError')
    }

    void executeAndCheck(String value)
    {
        def operation = operationModelBase.execute {
            operationName = "ErrorProcessing"
            presetValues = [ 'name':value ]
        }

        assertEquals(OperationStatus.ERROR, operation.getStatus())
    }
}