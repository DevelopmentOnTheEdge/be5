package com.developmentontheedge.be5.operation.services

import com.developmentontheedge.be5.base.exceptions.Be5ErrorCode
import com.developmentontheedge.be5.base.exceptions.Be5Exception
import com.developmentontheedge.be5.operation.OperationBe5ProjectDBTest
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue
import static org.junit.Assert.fail

class OperationServiceTransactionalTest extends OperationBe5ProjectDBTest
{
    @Test
    void generateErrorTransactional()
    {
        try{
            def operation = createOperation("testtableAdmin", "All records",
                    "ErrorProcessingTransactional", "")
            executeOperation(operation, ['name':'generateError'])
            fail()
        }catch (Throwable e){
            assertTrue(e instanceof Be5Exception)
            assertEquals(Be5ErrorCode.INTERNAL_ERROR_IN_OPERATION, ((Be5Exception)e).code)
        }
    }
}