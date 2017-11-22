package com.developmentontheedge.be5.databasemodel.impl

import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.api.services.OperationExecutor
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.operation.OperationStatus
import com.developmentontheedge.be5.test.Be5ProjectTest
import org.junit.Test

import static org.junit.Assert.*


class OperationModelBaseTest extends Be5ProjectTest
{
    @Inject OperationExecutor operationExecutor
    @Inject Meta meta

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