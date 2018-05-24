package com.developmentontheedge.be5.api.services

import com.developmentontheedge.be5.exceptions.Be5Exception
import com.developmentontheedge.be5.databasemodel.DatabaseModel
import com.developmentontheedge.be5.operation.OperationContext
import com.developmentontheedge.be5.operation.OperationInfo
import com.developmentontheedge.be5.operation.OperationStatus
import com.developmentontheedge.be5.test.ServerBe5ProjectDBTest
import org.junit.Test

import javax.inject.Inject

import static org.junit.Assert.*


class OperationExecutorTest extends ServerBe5ProjectDBTest
{
    @Inject DatabaseModel database
    @Inject OperationsFactory operations
    @Inject OperationExecutor operationExecutor
    @Inject Meta meta

    @Test
    void execute()
    {
        def testtableAdmin = database.getEntity("testtableAdmin")

        def info = new OperationInfo(meta.getOperation("testtableAdmin", "TransactionTestOp"))
        def operation = operationExecutor.create(info, new OperationContext([] as String[], null, Collections.emptyMap()))

        testtableAdmin.removeAll()

        operationExecutor.execute(operation, [:])

        assertEquals OperationStatus.ERROR, operation.getStatus()
        assertEquals 0, testtableAdmin.count()
    }

    @Test(expected = Be5Exception)
    void executeWithDatabase()
    {
        operations.get("testtableAdmin", "TransactionTestOp").execute()
    }


}