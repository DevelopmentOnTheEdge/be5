package com.developmentontheedge.be5.query.impl.utils;

import com.developmentontheedge.sql.model.SqlQuery;
import org.junit.Test;

import static org.junit.Assert.*;

public class QueryUtilsTest
{
    @Test
    public void hasColumnWithLabel()
    {
        assertFalse(QueryUtils.hasColumnWithLabel(SqlQuery.parse(
                "select * from table"), "___ID"));

        assertTrue(QueryUtils.hasColumnWithLabel(SqlQuery.parse(
                "select id AS \"___ID\" from table"), "___ID"));

        assertTrue(QueryUtils.hasColumnWithLabel(SqlQuery.parse(
                "select id AS \"___id\" from table"), "___ID"));
    }

}
