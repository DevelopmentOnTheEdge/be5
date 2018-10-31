package com.developmentontheedge.be5.query.impl.utils;

import com.developmentontheedge.be5.query.QueryBe5ProjectDBTest;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;
import org.junit.Test;

import static org.junit.Assert.*;

public class QueryUtilsTest extends QueryBe5ProjectDBTest
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

    @Test
    public void resolveTypeOfRefColumn()
    {
        QueryUtils.resolveTypeOfRefColumn(SqlQuery.parse("select * from filterTestTable t " +
              "WHERE CONCAT(t.firstName, ' ', t.lastName) LIKE CONCAT('%<parameter:queryString/>%')"), "filterTestTable", meta);
    }

    @Test
    public void notResolveForColumnInAnyFunction()
    {
        AstStart sql = SqlQuery.parse("select * from classifications " +
                "WHERE FORMAT_DATE(creationDate___) = '<parameter:creationDate___ />'");
        QueryUtils.resolveTypeOfRefColumn(sql, "classifications", meta);
        assertEquals("SELECT * FROM classifications " +
                "WHERE FORMAT_DATE(creationDate___) = '<parameter:creationDate___ />'", sql.format());
    }
}
