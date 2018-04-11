package com.developmentontheedge.sql;

import com.developmentontheedge.sql.model.SqlQuery;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class ReplacementParametersTest
{

    @Test
    public void testSimple()
    {
        String query = "SELECT id FROM persons WHERE name = ?";
        assertEquals(query, SqlQuery.parse( query ).format());
    }

    @Test
    public void test2()
    {
        String query = "SELECT id FROM persons WHERE name = ? AND title = ?";
        assertEquals(query, SqlQuery.parse( query ).format());
    }

    @Test
    public void inConcat()
    {
        String query = "SELECT id FROM categories WHERE name = CONCAT('user.', ?)";
        assertEquals(query, SqlQuery.parse( query ).format());
    }

    @Ignore
    @Test(expected = IllegalArgumentException.class)
    public void testErrorInSelectList()
    {
        String query = "SELECT ? FROM persons";
        assertEquals(query, SqlQuery.parse( query ).format());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplacementParametersErrorInFrom()
    {
        String query = "SELECT name FROM ?";
        assertEquals(query, SqlQuery.parse( query ).format());
    }

}
