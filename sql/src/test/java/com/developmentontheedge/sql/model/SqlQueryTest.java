package com.developmentontheedge.sql.model;

import org.junit.Test;

import static org.junit.Assert.*;


public class SqlQueryTest
{
    @Test
    public void columnName_end()
    {
        String query = "SELECT end FROM table";
        assertEquals(query, SqlQuery.parse(query).format());

        String query2 = "SELECT t.end FROM table t";
        assertEquals(query2, SqlQuery.parse(query2).format());

        String query3 = "INSERT INTO table (end) VALUES ('test')";
        assertEquals(query3, SqlQuery.parse(query3).format());
    }

}