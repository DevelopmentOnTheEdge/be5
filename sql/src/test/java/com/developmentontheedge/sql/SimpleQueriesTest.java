package com.developmentontheedge.sql;

import static org.junit.Assert.*;

import org.junit.Test;

import com.developmentontheedge.sql.model.SqlQuery;

public class SimpleQueriesTest
{
    @Test
    public void testLineBreak()
    {
        String query = "SELECT a,\n'abc' AS \"b\" FROM table1";
        assertEquals(query, SqlQuery.parse( query ).format());
    }
}
