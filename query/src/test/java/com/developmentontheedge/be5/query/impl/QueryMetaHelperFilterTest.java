package com.developmentontheedge.be5.query.impl;

import com.developmentontheedge.be5.query.QueryBe5ProjectDBTest;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Collections;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;


public class QueryMetaHelperFilterTest extends QueryBe5ProjectDBTest
{
    @Inject private QueryMetaHelper queryMetaHelper;

    @Test
    public void empty()
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Simple").getFinalQuery());
        queryMetaHelper.applyFilters(ast, Collections.emptyMap());

        assertEquals("SELECT ft.name, ft.valueCol\n" +
                "FROM filterTestTable ft", ast.format());
    }

    @Test
    public void simpleFilterIntColumn()
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Simple").getFinalQuery());
        queryMetaHelper.applyFilters(ast, singletonMap("valueCol", Collections.singletonList("123")));

        assertEquals("SELECT ft.name, ft.valueCol\n" +
                "FROM filterTestTable ft WHERE ft.valueCol = 123", ast.format());
    }

    @Test
    public void selectAllFilterIntColumn()
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Select all").getFinalQuery());
        queryMetaHelper.applyFilters(ast, singletonMap("valueCol", Collections.singletonList("123")));

        assertEquals("SELECT *\n" +
                "FROM filterTestTable ft WHERE ft.valueCol = 123", ast.format());
    }

    @Test
    public void selectAllFilterByID()
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Select all").getFinalQuery());
        queryMetaHelper.applyFilters(ast, singletonMap("ID", Collections.singletonList("1")));

        assertEquals("SELECT *\n" +
                "FROM filterTestTable ft WHERE ft.ID = 1", ast.format());
    }

    @Test
    public void selectAllWithSchemaFilterByID()
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("public.filterTestTable2", "All records").getFinalQuery());
        queryMetaHelper.applyFilters(ast, singletonMap("ID", Collections.singletonList("1")));

        assertEquals("SELECT *\n" +
                "FROM public.filterTestTable2 ft WHERE ft.ID = 1", ast.format());
    }

    @Test
    public void filterStringColumn()
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Simple").getFinalQuery());
        queryMetaHelper.applyFilters(ast, singletonMap("name", Collections.singletonList("test")));

        assertEquals("SELECT ft.name, ft.valueCol\n" +
                "FROM filterTestTable ft WHERE UPPER(ft.name) LIKE UPPER('%test%')", ast.format());
    }

    @Test
    public void filterStringColumnWithReference()
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Simple").getFinalQuery());
        queryMetaHelper.applyFilters(ast, singletonMap("user_name", Collections.singletonList("test")));

        assertEquals("SELECT ft.name, ft.valueCol\n" +
                "FROM filterTestTable ft WHERE ft.user_name ='test'", ast.format());
    }

    @Test
    public void filterStringColumnWithGenericReferences()
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Simple").getFinalQuery());
        queryMetaHelper.applyFilters(ast, singletonMap("recordID", Collections.singletonList("test")));

        assertEquals("SELECT ft.name, ft.valueCol\n" +
                "FROM filterTestTable ft WHERE ft.recordID ='test'", ast.format());
    }

    @Test
    public void filterStringColumnWithTags()
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Simple").getFinalQuery());
        queryMetaHelper.applyFilters(ast, singletonMap("activeStatus", Collections.singletonList("yes")));

        assertEquals("SELECT ft.name, ft.valueCol\n" +
                "FROM filterTestTable ft WHERE ft.activeStatus ='yes'", ast.format());
    }

    @Test
    public void ignoreKeywords()
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Simple").getFinalQuery());
        queryMetaHelper.applyFilters(ast, singletonMap("_test_", Collections.singletonList("test")));

        assertEquals("SELECT ft.name, ft.valueCol\n" +
                "FROM filterTestTable ft", ast.format());
    }

    @Test
    public void isNotContainsInQuery()
    {
        assertEquals(true, queryMetaHelper.isNotContainsInQuery(
                "filterTestTable", of("ft", "filterTestTable"), "testtable.productID"));

        assertEquals(true, queryMetaHelper.isNotContainsInQuery(
                "filterTestTable", of("ft", "filterTestTable"), "testtable.productID"));
    }

    @Test
    public void isNotContainsInQueryUnknownAlias()
    {
        assertEquals(true, queryMetaHelper.isNotContainsInQuery(
                "filterTestTable", of(), "t.productID"));
    }

    @Test
    public void isNotContainsInQueryFalse()
    {
        assertEquals(false, queryMetaHelper.isNotContainsInQuery(
                "filterTestTable", of(), "filterTestTable.name"));
        assertEquals(false, queryMetaHelper.isNotContainsInQuery(
                "filterTestTable", of("ft", "filterTestTable"), "ft.name"));
    }

    @Test
    public void ignoreUsedParams()
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "With Parameter").getFinalQuery());
        queryMetaHelper.applyFilters(ast, singletonMap("name", Collections.singletonList("test")));

        assertEquals("SELECT ft.name, ft.valueCol\n" +
                "FROM filterTestTable ft\n" +
                "WHERE (1 = 1)\n" +
                "<if parameter=\"name\">\n" +
                "  AND ft.name LIKE <parameter:name />\n" +
                "</if>", ast.format());
    }

}
