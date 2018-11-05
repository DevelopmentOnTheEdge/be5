package com.developmentontheedge.be5.metadata.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class EntityTest
{
    @Test
    public void testClone() throws Exception
    {
        Project prj = new Project("test");
        Entity e = new Entity("e", prj.getApplication(), EntityType.TABLE);
        DataElementUtils.save(e);
        Query query = new Query("query", e);
        DataElementUtils.save(query);
        Operation op = Operation.createOperation("op", Operation.OPERATION_TYPE_JAVA, e);
        DataElementUtils.save(op);

        Entity e2 = (Entity) e.clone(e.getOrigin(), e.getName());
        TestHelpers.checkEquality(e, e2);

        op.setRecords(Operation.VISIBLE_WHEN_ANY_SELECTED_RECORDS);
        assertNotEquals(e, e2);
        op.setRecords(Operation.VISIBLE_ALWAYS);
        assertEquals(e, e2);
    }
}
