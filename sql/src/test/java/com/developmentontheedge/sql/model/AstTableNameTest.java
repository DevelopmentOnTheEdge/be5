package com.developmentontheedge.sql.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class AstTableNameTest
{
    @Test
    public void table() throws Exception {
        assertEquals("users",
                new AstTableName("users").toString());
    }

    @Test
    public void withSchema() throws Exception {
        assertEquals("public.users",
                new AstTableName("public", "users").toString());

        assertEquals("public.users",
                new AstTableName("public.users").toString());
    }

    @Test
    public void testSetValue() throws Exception {
        assertEquals("public.users",
                new AstTableName("users").setValue("public.users").toString());

        assertEquals("users",
                new AstTableName("users").setValue("users").toString());
    }

    @Test(expected = AssertionError.class)
    public void testSetValueException() throws Exception {
        new AstTableName("users").setValue("public.users.table");
    }

}