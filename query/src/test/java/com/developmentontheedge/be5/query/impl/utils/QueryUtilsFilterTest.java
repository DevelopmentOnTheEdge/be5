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
        QueryUtils.applyFilters(ast, "filterTestTable", Collections.emptyMap(), meta);

        assertEquals("SELECT ft.name, ft.value\n" +
                "FROM filterTestTable ft", ast.format());
    }

    @Test
    public void simpleFilterIntColumn() throws Exception
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Simple").getQueryCompiled().validate().trim());
        QueryUtils.applyFilters(ast, "filterTestTable", Collections.singletonMap("value", Collections.singletonList("123")), meta);

        assertEquals("SELECT ft.name, ft.value\n" +
                "FROM filterTestTable ft WHERE ft.value = 123", ast.format());
    }

    @Test
    public void selectAllFilterIntColumn() throws Exception
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Select all").getQueryCompiled().validate().trim());
        QueryUtils.applyFilters(ast, "filterTestTable", Collections.singletonMap("value", Collections.singletonList("123")), meta);

        assertEquals("SELECT *\n" +
                "FROM filterTestTable ft WHERE ft.value = 123", ast.format());
    }

    @Test
    public void selectAllFilterByID() throws Exception
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Select all").getQueryCompiled().validate().trim());
        QueryUtils.applyFilters(ast, "filterTestTable", Collections.singletonMap("ID", Collections.singletonList("1")), meta);

        assertEquals("SELECT *\n" +
                "FROM filterTestTable ft WHERE ft.ID = 1", ast.format());
    }

    @Test
    public void selectAllWithSchemaFilterByID() throws Exception
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("public.filterTestTable2", "All records").getQueryCompiled().validate().trim());
        QueryUtils.applyFilters(ast, "public.filterTestTable2", Collections.singletonMap("ID", Collections.singletonList("1")), meta);

        assertEquals("SELECT *\n" +
                "FROM public.filterTestTable2 ft WHERE ft.ID = 1", ast.format());
    }

    @Test
    public void filterStringColumn() throws Exception
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Simple").getQueryCompiled().validate().trim());
        QueryUtils.applyFilters(ast, "filterTestTable", Collections.singletonMap("name", Collections.singletonList("test")), meta);

        assertEquals("SELECT ft.name, ft.value\n" +
                "FROM filterTestTable ft WHERE UPPER(ft.name) LIKE UPPER('%test%')", ast.format());
    }

    @Test
    public void filterStringColumnWithReference()
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Simple").getQuery());
        QueryUtils.applyFilters(ast, "filterTestTable", Collections.singletonMap("user_name", Collections.singletonList("test")), meta);

        assertEquals("SELECT ft.name, ft.value\n" +
                "FROM filterTestTable ft WHERE ft.user_name ='test'", ast.format());
    }

    @Test
    public void filterStringColumnWithGenericReferences()
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Simple").getQuery());
        QueryUtils.applyFilters(ast, "filterTestTable", Collections.singletonMap("recordID", Collections.singletonList("test")), meta);

        assertEquals("SELECT ft.name, ft.value\n" +
                "FROM filterTestTable ft WHERE ft.recordID ='test'", ast.format());
    }

    @Test
    public void filterStringColumnWithTags()
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Simple").getQuery());
        QueryUtils.applyFilters(ast, "filterTestTable", Collections.singletonMap("activeStatus", Collections.singletonList("yes")), meta);

        assertEquals("SELECT ft.name, ft.value\n" +
                "FROM filterTestTable ft WHERE ft.activeStatus ='yes'", ast.format());
    }

    @Test
    public void ignoreKeywords() throws Exception
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Simple").getQueryCompiled().validate().trim());
        QueryUtils.applyFilters(ast, "filterTestTable", Collections.singletonMap("_test_", Collections.singletonList("test")), meta);

        assertEquals("SELECT ft.name, ft.value\n" +
                "FROM filterTestTable ft", ast.format());
    }

    @Test
    public void isNotContainsInQuery() throws Exception
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Simple").getQueryCompiled().validate().trim());
        QueryUtils.applyFilters(ast, "filterTestTable", Collections.singletonMap("t.productID", Collections.singletonList("1")), meta);

        assertEquals("SELECT ft.name, ft.value\n" +
                "FROM filterTestTable ft", ast.format());
    }

    @Test
    public void ignoreUsedParams() throws Exception
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "With Parameter").getQueryCompiled().validate().trim());
        QueryUtils.applyFilters(ast, "filterTestTable", Collections.singletonMap("name", Collections.singletonList("test")), meta);

        assertEquals("SELECT ft.name, ft.value\n" +
                "FROM filterTestTable ft\n" +
                "WHERE (1 = 1)\n" +
                "<if parameter=\"name\">\n" +
                "  AND ft.name LIKE <parameter:name />\n" +
                "</if>", ast.format());
    }
}
