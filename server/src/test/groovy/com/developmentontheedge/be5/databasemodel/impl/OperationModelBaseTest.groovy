package com.developmentontheedge.be5.databasemodel.impl

import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.api.services.OperationExecutor
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.operation.OperationStatus
import com.developmentontheedge.be5.test.Be5ProjectTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

import static org.junit.Assert.*


class OperationModelBaseTest extends Be5ProjectTest
{
    @Inject OperationExecutor operationExecutor
    @Inject Meta meta

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    void execute()
    {
        def operationModelBase = new OperationModelBase(meta, operationExecutor)
        operationModelBase.setEntityName("testtableAdmin")
        operationModelBase.setQueryName("All records")

        operationModelBase.with {
            operationName = "ErrorProcessing"
            presetValues  = [ 'name':'ok' ]
        }
        def operation = operationModelBase.execute()

        assertEquals(OperationStatus.FINISHED, operation.getStatus())
    }

    @Test
    void errorInGenerate()
    {
        expectedEx.expect(RuntimeException.class)
        expectedEx.expectMessage("The operation can not be performed.")

        def operationModelBase = new OperationModelBase(meta, operationExecutor)
        operationModelBase.setEntityName("testtableAdmin")
        operationModelBase.setQueryName("All records")

        operationModelBase.with {
            operationName = "ErrorProcessing"
            presetValues  = [ 'name':'generateErrorStatus' ]
        }
        def operation = operationModelBase.execute()

        assertEquals(OperationStatus.ERROR, operation.getStatus())
    }

}