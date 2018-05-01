package com.developmentontheedge.be5.databasemodel.impl;

import com.developmentontheedge.be5.databasemodel.EntityModel;
import com.developmentontheedge.be5.databasemodel.RecordModel;
import com.developmentontheedge.be5.test.Be5ProjectDBTest;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class EntityModelBaseTest extends Be5ProjectDBTest
{
    private EntityModel<Long> testtableAdmin;

    @Before
    public void before()
    {
        testtableAdmin = database.getEntity("testtableAdmin");
        db.update("DELETE FROM testtableAdmin");
    }

    @Test
    public void testUpdate()
    {
        Long id = testtableAdmin.add(ImmutableMap.of(
            "name", "TestName",
            "value", 1
        ));

        testtableAdmin.set(id, ImmutableMap.of(
            "name", "TestName2"
        ));

        RecordModel<Long> record = database.<Long>getEntity("testtableAdmin").get(id);

        Long id2 = record.getPrimaryKey();
        assertEquals(Long.class, id2.getClass());

        assertEquals( "TestName2", record.getValue("name"));
        assertEquals( "TestName2", testtableAdmin.get(id).getValue("name"));

        record.update( ImmutableMap.of(
            "name", "TestName3"
        ));
    }

}