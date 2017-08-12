package com.developmentontheedge.sql.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class AstDeleteTest
{

    @Test
    public void testAll()
    {
        String query = "DELETE FROM Customers";
        assertEquals(query, SqlQuery.parse( query ).format());
    }

    @Test
    public void testWhere()
    {
        String query = "DELETE FROM Customers " +
                "WHERE CustomerName = \"Alfreds Futterkiste\"";
        assertEquals(query, SqlQuery.parse( query ).format());
    }

    @Test
    public void testWhereReplacementParameter()
    {
        String query = "DELETE FROM Customers WHERE CustomerName = ?";
        assertEquals(query, SqlQuery.parse( query ).format());
    }

    @Test
    public void testWhereReplacementParameterIn()
    {
        String query = "DELETE FROM Customers WHERE CustomerName IN (?)";
        assertEquals(query, SqlQuery.parse( query ).format());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testError()
    {
        String query = "DELETE FROM WHERE CustomerName = ?";
        assertEquals(query, SqlQuery.parse( query ).format());
    }
}