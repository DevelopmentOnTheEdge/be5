package com.developmentontheedge.sql;

import com.developmentontheedge.sql.model.SqlQuery;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimpleQueriesTest
{
    @Test
    public void testLineBreak()
    {
        String query = "SELECT a,\n'abc' AS \"b\" FROM table1";
        assertEquals(query, SqlQuery.parse(query).format());
    }
}
