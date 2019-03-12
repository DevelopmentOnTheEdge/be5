package com.developmentontheedge.be5.operation.services;

import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationBe5ProjectDBTest;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationStatus;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Collections;

import static com.developmentontheedge.be5.operation.OperationConstants.SELECTED_ROWS;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

public class OperationExecutorTest extends OperationBe5ProjectDBTest
{
    @Inject private OperationBuilder.OperationsFactory operations;
    @Inject private OperationExecutor operationExecutor;
    @Inject private Meta meta;

    @Test
    public void execute()
    {
        OperationInfo info = new OperationInfo(meta.getOperation("testtableAdmin", "TransactionTestOp"));
        Operation operation = operationExecutor.create(info, operationExecutor.getOperationContext(info, null, Collections.emptyMap()));

        db.update("DELETE FROM testtableAdmin");

        operationExecutor.execute(operation, emptyMap());

        assertEquals(OperationStatus.ERROR, operation.getStatus());
        assertEquals(0L, (long) db.oneLong("SELECT count(1) FROM testtableAdmin"));
    }

    @Test(expected = Be5Exception.class)
    public void executeWithDatabase()
    {
        operations.create("testtableAdmin", "TransactionTestOp").execute();
    }

    @Test
    public void oneRecord()
    {
        OperationInfo info = new OperationInfo(meta.getOperation("testtableAdmin", "TransactionTestOp"));
        Operation op = operationExecutor.create(info, "All records", singletonMap(SELECTED_ROWS, "1"));

        assertEquals(OperationStatus.CREATE, op.getStatus());
        assertEquals(1L, (long) op.getContext().getRecord());
    }

    @Test
    public void manyRecords()
    {
        OperationInfo info = new OperationInfo(meta.getOperation("testtableAdmin", "TransactionTestOp"));
        Operation op = operationExecutor.create(info, "All records",
                singletonMap(SELECTED_ROWS, new String[]{"1", "2"}));

        assertEquals(OperationStatus.CREATE, op.getStatus());
        Assert.assertArrayEquals(new Object[]{1L, 2L}, op.getContext().getRecords());
    }
}
