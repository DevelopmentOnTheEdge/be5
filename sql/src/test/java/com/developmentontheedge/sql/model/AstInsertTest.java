package com.developmentontheedge.sql.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class AstInsertTest
{
    @Test
    public void testOne()
    {
        String query = "INSERT INTO users (name) VALUES (\"Test\")";
        assertEquals(query, SqlQuery.parse( query ).format());
    }

    @Test
    public void test2()
    {
        String query = "INSERT INTO users (name, value) VALUES (\"Test\", 1)";
        assertEquals(query, SqlQuery.parse( query ).format());
    }

    @Test
    public void testReplacementParameter()
    {
        String query = "INSERT INTO users (name, value) VALUES (?, ?)";
        assertEquals(query, SqlQuery.parse( query ).format());
    }
}