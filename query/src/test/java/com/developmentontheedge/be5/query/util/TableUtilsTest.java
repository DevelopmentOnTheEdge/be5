package com.developmentontheedge.be5.query.util;

import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TableUtilsTest
{
    @Test
    public void countFromQuery()
    {
        AstStart sql = SqlQuery.parse("SELECT\n" +
                "  t.name AS \"Name\",\n" +
                "  t.value AS \"Value\"\n" +
                "FROM\n" +
                "  testtable t");
        QueryUtils.countFromQuery(sql.getQuery());

        assertEquals("SELECT COUNT(*) AS \"count\" FROM (SELECT\n" +
                "  t.name AS \"Name\",\n" +
                "  t.value AS \"Value\"\n" +
                "FROM\n" +
                "  testtable t) AS \"data\"", sql.format());
    }

}
