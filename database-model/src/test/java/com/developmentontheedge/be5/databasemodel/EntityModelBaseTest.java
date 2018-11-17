package com.developmentontheedge.be5.databasemodel;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;


public class EntityModelBaseTest extends DatabaseModelProjectDbTest
{
    @Inject
    private DatabaseModel database;

    @Before
    public void before()
    {
        db.update("DELETE FROM testtableAdmin");
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

    @Test(expected = IllegalArgumentException.class)
    public void get()
    {
        Long id = database.getEntity("testtableAdmin").add(ImmutableMap.of("name", "TestName", "value", 1));

        database.<String>getEntity("testtableAdmin").get(id.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void set()
    {
        Long id = database.getEntity("testtableAdmin").add(ImmutableMap.of("name", "TestName", "value", 1));

        database.<String>getEntity("testtableAdmin").set(id.toString(), ImmutableMap.of("value", 2));
    }
}
