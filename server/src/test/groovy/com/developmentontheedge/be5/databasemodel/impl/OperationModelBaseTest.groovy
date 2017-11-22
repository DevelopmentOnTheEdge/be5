package com.developmentontheedge.be5.databasemodel.impl

import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.api.services.OperationExecutor
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.operation.OperationStatus
import com.developmentontheedge.be5.test.Be5ProjectTest
import com.developmentontheedge.be5.test.mocks.SqlServiceMock
import org.junit.Test

import static org.junit.Assert.*
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.verify

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
            operationName = "TestGroovyOp"
            presetValues = [
                    'beginDate'  : '2017-12-20',
                    'name'       : 'testValue',
                    'reason'     : 'fired',
                    'reasonMulti': ['fired', 'other'] as String[]
            ]
        }
        def operation = operationModelBase.execute()

        assertEquals OperationStatus.REDIRECTED, operation.getStatus()

        verify(SqlServiceMock.mock).update(eq("update fakeTable set name = ?,beginDate = ?,reason = ?"),
                eq("testValue"),
                eq(parseDate("2017-07-01")),
                eq("fired"))
    }

}