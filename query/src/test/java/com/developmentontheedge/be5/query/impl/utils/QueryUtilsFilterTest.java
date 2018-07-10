package com.developmentontheedge.be5.query.impl.utils;

import com.developmentontheedge.be5.query.QueryBe5ProjectDBTest;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;


public class QueryUtilsFilterTest extends QueryBe5ProjectDBTest
{
    @Test
    public void empty() throws Exception
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Simple").getQueryCompiled().validate().trim());
        QueryUtils.applyFilters(ast, "filterTestTable", Collections.emptyMap());

        assertEquals("SELECT ft.name, ft.value\n" +
                "FROM filterTestTable ft", ast.format());
    }

    @Test
    public void simpleFilterIntColumn() throws Exception
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Simple").getQueryCompiled().validate().trim());
        QueryUtils.applyFilters(ast, "filterTestTable", Collections.singletonMap("value", Collections.singletonList(123)));

        assertEquals("SELECT ft.name, ft.value\n" +
                "FROM filterTestTable ft WHERE ft.value = 123", ast.format());
    }

    @Test
    public void simpleFilterStringColumn() throws Exception
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Simple").getQueryCompiled().validate().trim());
        QueryUtils.applyFilters(ast, "filterTestTable", Collections.singletonMap("name", Collections.singletonList("test")));

        assertEquals("SELECT ft.name, ft.value\n" +
                "FROM filterTestTable ft WHERE ft.name ='test'", ast.format());
    }

    @Test
    public void ignoreKeywords() throws Exception
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Simple").getQueryCompiled().validate().trim());
        QueryUtils.applyFilters(ast, "filterTestTable", Collections.singletonMap("_search_", Collections.singletonList("test")));

        assertEquals("SELECT ft.name, ft.value\n" +
                "FROM filterTestTable ft", ast.format());
    }

    @Test
    public void ignoreUsedParams() throws Exception
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "With Parameter").getQueryCompiled().validate().trim());
        QueryUtils.applyFilters(ast, "filterTestTable", Collections.singletonMap("name", Collections.singletonList("test")));

        assertEquals("SELECT ft.name, ft.value\n" +
                "FROM filterTestTable ft\n" +
                "WHERE (1 = 1)\n" +
                "<if parameter=\"name\">\n" +
                "  AND ft.name LIKE <parameter:name />\n" +
                "</if>", ast.format());
    }
}