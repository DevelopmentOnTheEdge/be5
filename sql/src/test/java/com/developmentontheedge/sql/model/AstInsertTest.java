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
    public void testWithoutColumnNames()
    {
        String query = "INSERT INTO user_prefs VALUES ( ?, ?, ? )";
        assertEquals(query, SqlQuery.parse( query ).format());
    }

    @Test
    public void test2()
    {
        String query = "INSERT INTO users (name, value) VALUES (\"Test\", 1)";
        assertEquals(query, SqlQuery.parse( query ).format());
    }

    @Test
    public void testEscapedColumns()
    {
        String query = "INSERT INTO users (\"___name\", value) VALUES (\"Test\", 1)";
        assertEquals(query, SqlQuery.parse( query ).format());
    }

    @Test
    public void testReplacementParameter()
    {
        String query = "INSERT INTO users (name, value) VALUES (?, ?)";
        assertEquals(query, SqlQuery.parse( query ).format());
    }

    @Test
    public void insertSelect()
    {
        String query = "INSERT INTO table2 SELECT * FROM table1 WHERE 1 = 1";
        assertEquals(query, SqlQuery.parse( query ).format());
    }

    @Test
    public void insertSelect2()
    {
        String query = "INSERT INTO table2 (name, value) " +
                "SELECT name, value FROM table1";
        assertEquals(query, SqlQuery.parse( query ).format());
    }
}