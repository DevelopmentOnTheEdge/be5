package com.developmentontheedge.be5.api.services.impl

import com.developmentontheedge.be5.api.exceptions.Be5Exception
import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.api.services.OperationExecutor
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel
import com.developmentontheedge.be5.operation.OperationInfo
import com.developmentontheedge.be5.operation.OperationStatus
import com.developmentontheedge.be5.test.Be5ProjectDBTest
import org.junit.Test

import javax.inject.Inject

import static org.junit.Assert.*


class OperationExecutorImplTest extends Be5ProjectDBTest
{
    @Inject DatabaseModel database
    @Inject OperationExecutor operationExecutor
    @Inject Meta meta

    @Test
    void execute()
    {
        def testtableAdmin = database.getEntity("testtableAdmin")

        def info = new OperationInfo("All records",
                meta.getOperationIgnoringRoles("testtableAdmin", "TransactionTestOp"))
        def operation = operationExecutor.create(info, [] as String[])

        testtableAdmin.removeAll()

        operationExecutor.execute(operation, [:])

        assertEquals OperationStatus.ERROR, operation.getStatus()
        assertEquals 0, testtableAdmin.count()
    }

    @Test(expected = Be5Exception)
    void executeWithDatabase()
    {
        database.getEntity("testtableAdmin").getOperation("TransactionTestOp").execute()
    }


}