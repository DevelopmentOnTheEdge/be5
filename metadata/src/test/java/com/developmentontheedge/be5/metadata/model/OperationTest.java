package com.developmentontheedge.be5.metadata.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OperationTest
{
    @Test
    public void testBasics()
    {
        Project prj = new Project("test");
        Entity e = new Entity("e", prj.getApplication(), EntityType.TABLE);
        DataElementUtils.save(e);
        for (String type : Operation.getOperationTypes()) {
            String name = "op of type " + type;
            Operation operation = Operation.createOperation(name, type, e);
            operation.setCode("code");
            assertEquals(name, operation.getName());
            assertEquals(type, operation.getType());
            assertEquals("operation", operation.getEntityItemType());
            assertEquals("code", operation.getCode());
        }
        Operation operation = Operation.createOperation("test", Operation.OPERATION_TYPE_JAVA, e);
        operation.setRecords(Operation.VISIBLE_WHEN_ANY_SELECTED_RECORDS);
        assertEquals("When any number of records is selected", operation.getVisibleWhen());
        operation.setVisibleWhen("Always");
        assertEquals(Operation.VISIBLE_ALWAYS, operation.getRecords());

        assertTrue(operation.getErrors().isEmpty());
        operation.setRecords(1234);
        assertEquals(1, operation.getErrors().size());
    }

    @Test
    public void testPrototype()
    {
        Project prj = new Project("test");
        Entity e = new Entity("e", prj.getApplication(), EntityType.TABLE);
        DataElementUtils.save(e);
        Operation prototype = Operation.createOperation("test", Operation.OPERATION_TYPE_JAVA, e);
        prototype.setRecords(Operation.VISIBLE_ALL_OR_SELECTED);
        prototype.setNotSupported("not supported string");
        prototype.setExecutionPriority(0);
        prototype.setConfirm(true);
        Operation op = Operation.createOperation("test", Operation.OPERATION_TYPE_JAVA, e);
        assertEquals(999999, op.getExecutionPriority());
        op.merge(prototype, false, true);
        assertEquals(0, op.getExecutionPriority());
        assertEquals(Operation.VISIBLE_ALL_OR_SELECTED, op.getRecords());
        assertEquals("not supported string", op.getNotSupported());
        assertTrue(op.isConfirm());
        op.setNotSupported("NOOOO");
        assertEquals("NOOOO", op.getNotSupported());
        op.inheritProperty("notSupported");
        assertEquals("not supported string", op.getNotSupported());
    }

    @Test
    public void testClone() throws Exception
    {
        Project prj = new Project("test");
        Entity e = new Entity("e", prj.getApplication(), EntityType.TABLE);
        DataElementUtils.save(e);
        Operation op = Operation.createOperation("op", Operation.OPERATION_TYPE_JAVA, e);
        DataElementUtils.save(op);
        Operation op2 = (Operation) op.clone(op.getOrigin(), op.getName());
        TestHelpers.checkEquality(op, op2);
    }
}
