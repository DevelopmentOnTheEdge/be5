package com.developmentontheedge.be5.operation.services;

import com.developmentontheedge.be5.exceptions.Be5ErrorCode;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationBe5ProjectDBTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OperationServiceTransactionalTest extends OperationBe5ProjectDBTest
{
    @Test
    public void generateErrorTransactional()
    {
        try
        {
            Operation operation = createOperation("testtableAdmin", "All records",
                    "ErrorProcessingTransactional", "");
            executeOperation(operation, Collections.singletonMap("name", "generateError"));
            Assert.fail();
        }
        catch (Throwable e)
        {
            assertTrue(e instanceof Be5Exception);
            assertEquals(Be5ErrorCode.INTERNAL_ERROR_IN_OPERATION, ((Be5Exception) e).getCode());
        }

    }

}
