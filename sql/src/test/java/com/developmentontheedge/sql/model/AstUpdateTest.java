package com.developmentontheedge.sql.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AstUpdateTest
{
    @Test
    public void testAll()
    {
        String query = "UPDATE Customers\n" +
                "SET ContactName = 'Alfred Schmidt', City = 'Frankfurt'";
        assertEquals(query, SqlQuery.parse( query ).format());
    }

    @Test
    public void testOne()
    {
        String query = "UPDATE Customers\n" +
                "SET ContactName = 'Alfred Schmidt', City = 'Frankfurt'\n" +
                "WHERE CustomerID = 1";
        assertEquals(query, SqlQuery.parse( query ).format());
    }

    @Test
    public void testWhereReplacementParameter()
    {
        String query = "UPDATE Customers\n" +
                "SET ContactName = ?, CityID = 123\n" +
                "WHERE CustomerID = ?";
        assertEquals(query, SqlQuery.parse( query ).format());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testError()
    {
        String query = "UPDATE Customers\n" +
                "SET ContactName > 'Alfred Schmidt', City = 'Frankfurt'";
        assertEquals(query, SqlQuery.parse( query ).format());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testError2()
    {
        String query = "UPDATE SET ContactName = 'Alfred Schmidt'";
        assertEquals(query, SqlQuery.parse( query ).format());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptySet()
    {
        String query = "UPDATE Customers SET";
        assertEquals(query, SqlQuery.parse( query ).format());
    }
}