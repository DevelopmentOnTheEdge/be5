package com.developmentontheedge.be5.query.impl.utils;

import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;


public class QueryUtilsFilterTest
{
    Meta meta;

    @Test
    @Ignore
    public void empty() throws Exception
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Simple").getQueryCompiled().validate().trim());
        QueryUtils.applyFilters(ast, "filterTestTable", Collections.emptyMap());

        assertEquals("SELECT ft.name, ft.value\n" +
                "FROM filterTestTable ft", ast.format());
    }

    @Test
    @Ignore
    public void simpleFilterIntColumn() throws Exception
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Simple").getQueryCompiled().validate().trim());
        QueryUtils.applyFilters(ast, "filterTestTable", Collections.singletonMap("value", Collections.singletonList(123)));

        assertEquals("SELECT ft.name, ft.value\n" +
                "FROM filterTestTable ft WHERE ft.value = 123", ast.format());
    }

    @Test
    @Ignore
    public void simpleFilterStringColumn() throws Exception
    {
        AstStart ast = SqlQuery.parse(meta.getQuery("filterTestTable", "Simple").getQueryCompiled().validate().trim());
        QueryUtils.applyFilters(ast, "filterTestTable", Collections.singletonMap("name", Collections.singletonList("test")));

        assertEquals("SELECT ft.name, ft.value\n" +
                "FROM filterTestTable ft WHERE ft.name ='test'", ast.format());
    }
}