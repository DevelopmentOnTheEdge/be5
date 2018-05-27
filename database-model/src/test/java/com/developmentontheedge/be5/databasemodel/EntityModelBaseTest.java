package com.developmentontheedge.be5.databasemodel;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;


public class EntityModelBaseTest extends DatabaseModelProjectDbTest
{
    @Inject private DatabaseModel database;

    @Before
    public void before()
    {
        db.update("DELETE FROM testtableAdmin");
    }

    @Test
    public void testUpdate()
    {
        EntityModel<Long> testtableAdmin = database.getEntity("testtableAdmin");

        Long id = testtableAdmin.add(ImmutableMap.of(
            "name", "TestName",
            "value", 1
        ));

        testtableAdmin.set(id, ImmutableMap.of(
            "name", "TestName2"
        ));

        RecordModel<Long> record = testtableAdmin.get(id);

        Long id2 = record.getPrimaryKey();
        assertEquals(Long.class, id2.getClass());

        assertEquals( "TestName2", record.getValue("name"));
        assertEquals( "TestName2", testtableAdmin.get(id).getValue("name"));

        record.update( ImmutableMap.of(
            "name", "TestName3"
        ));
    }

    @Test
    public void add()
    {
        Long id = database.getEntity("testtableAdmin").add(ImmutableMap.of(
                "name", "TestName",
                "value", 1
        ));
        assertEquals(Long.class, id.getClass());
    }

    @Test(expected = RuntimeException.class)
    public void get()
    {
        Long id = database.getEntity("testtableAdmin").add(ImmutableMap.of( "name", "TestName", "value", 1 ));

        RecordModel<String> record = database.<String>getEntity("testtableAdmin").get(id.toString());
    }

    @Test(expected = RuntimeException.class)
    public void set()
    {
        Long id = database.getEntity("testtableAdmin").add(ImmutableMap.of( "name", "TestName", "value", 1 ));

        int count = database.<String>getEntity("testtableAdmin").set(id.toString(), ImmutableMap.of("value", 2));
    }

}