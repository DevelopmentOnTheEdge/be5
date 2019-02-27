package com.developmentontheedge.be5.operation.services

import com.developmentontheedge.be5.exceptions.Be5Exception
import com.developmentontheedge.be5.meta.Meta
import com.developmentontheedge.be5.operation.OperationBe5ProjectDBTest
import com.developmentontheedge.be5.operation.OperationConstants
import com.developmentontheedge.be5.operation.OperationInfo
import com.developmentontheedge.be5.operation.OperationStatus
import org.junit.Test

import javax.inject.Inject

import static org.junit.Assert.assertArrayEquals
import static org.junit.Assert.assertEquals

class OperationExecutorTest extends OperationBe5ProjectDBTest
{
    @Inject
    OperationBuilder.OperationsFactory operations
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
        operations.create("testtableAdmin", "TransactionTestOp").execute()
    }

    @Test
    void oneRecord()
    {
        def op = operationExecutor.create(
                new OperationInfo(meta.getOperation("testtableAdmin", "TransactionTestOp")),
                "All records", Collections.singletonMap(OperationConstants.SELECTED_ROWS, "1"))

        assertEquals(OperationStatus.CREATE, op.getStatus())
        assertEquals(1L, op.getContext().getRecord())
    }

    @Test
    void manyRecords()
    {
        def op = operationExecutor.create(
                new OperationInfo(meta.getOperation("testtableAdmin", "TransactionTestOp")),
                "All records", Collections.singletonMap(OperationConstants.SELECTED_ROWS, [1, 2] as String[]))

        assertEquals(OperationStatus.CREATE, op.getStatus())
        assertArrayEquals([1L, 2L] as Object[], op.getContext().getRecords())
    }
}
