package com.developmentontheedge.be5.operation.services

import com.developmentontheedge.be5.base.exceptions.Be5Exception
import com.developmentontheedge.be5.base.services.Meta
import com.developmentontheedge.be5.operation.OperationBe5ProjectDBTest
import com.developmentontheedge.be5.operation.OperationConstants
import com.developmentontheedge.be5.operation.model.OperationInfo
import com.developmentontheedge.be5.operation.model.OperationStatus
import org.junit.Test

import javax.inject.Inject

import static org.junit.Assert.assertEquals

class OperationExecutorTest extends OperationBe5ProjectDBTest
{
    @Inject
    OperationsFactory operations
    @Inject
    OperationExecutor operationExecutor
    @Inject
    Meta meta

    @Test
    void execute()
    {
        def info = new OperationInfo(meta.getOperation("testtableAdmin", "TransactionTestOp"))
        def operation = operationExecutor.create(info,
                operationExecutor.getOperationContext(info, null, Collections.emptyMap()))

        db.update("DELETE FROM testtableAdmin")

        operationExecutor.execute(operation, [:])

        assertEquals OperationStatus.ERROR, operation.getStatus()
        assertEquals 0, db.oneLong("SELECT count(1) FROM testtableAdmin")
    }

    @Test(expected = Be5Exception)
    void executeWithDatabase()
    {
        operations.get("testtableAdmin", "TransactionTestOp").execute()
    }

    @Test
    void create()
    {
        def op = operationExecutor.create(
                new OperationInfo(meta.getOperation("testtableAdmin", "TransactionTestOp")),
                "All records", Collections.singletonMap(OperationConstants.SELECTED_ROWS, "1"))

        assertEquals(OperationStatus.CREATE, op.getStatus())
        assertEquals(1L, op.getContext().getRecord())
    }
}
