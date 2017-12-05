package com.developmentontheedge.be5.databasemodel.impl

import com.developmentontheedge.be5.api.exceptions.Be5Exception
import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.api.services.OperationExecutor
import com.developmentontheedge.be5.databasemodel.OperationModel
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.operation.OperationStatus
import com.developmentontheedge.be5.test.Be5ProjectTest
import com.developmentontheedge.beans.json.JsonFactory
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

    OperationModel operationModelBase

    @Before
    void init()
    {
        operationModelBase = new OperationModelBase(meta, operationExecutor)
                .setEntityName("testtableAdmin").setQueryName("All records")
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
    void generate()
    {
        def parameters = operationModelBase.generate {
            records       = ["1"]
            presetValues  = [ 'name': 'ok' ]
            operationName = "ErrorProcessing"
        }

        assertEquals("{'values':{'name':'ok','propertyForAnotherEntity':'text'},'meta':{'/name':{'displayName':'name'},'/propertyForAnotherEntity':{'displayName':'propertyForAnotherEntity'}},'order':['/name','/propertyForAnotherEntity']}",
                oneQuotes(JsonFactory.bean(parameters)))
    }

    @Test
    void generateErrorInPropertyOnExecute()
    {
        expectedEx.expect(IllegalArgumentException.class)
        expectedEx.expectMessage("Error in property (getParameters)")
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
        expectedEx.expectMessage("Error in property (invoke)")
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
        expectedEx.expect(Be5Exception.class)
        expectedEx.expectMessage("Internal error occured during operation testtableAdmin.ErrorProcessing")
        executeAndCheck('executeError')
    }

    @Test
    void executeOperationWithoutParams()
    {
        expectedEx.expect(Be5Exception.class)
        expectedEx.expectMessage("Internal error occured during operation testtableAdmin.ErrorProcessing")
        executeAndCheck('withoutParams')
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