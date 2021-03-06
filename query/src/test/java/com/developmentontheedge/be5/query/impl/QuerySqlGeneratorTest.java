package com.developmentontheedge.be5.query.impl;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryBe5ProjectDBTest;
import com.google.common.collect.ImmutableMap;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.inject.Inject;
import java.util.Arrays;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

import static com.developmentontheedge.be5.query.QueryConstants.LIMIT;

public class QuerySqlGeneratorTest extends QueryBe5ProjectDBTest
{
    @Inject
    private QuerySqlGenerator querySqlGenerator;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testMultipleColumn()
    {
        Query query = meta.getQuery("testtable", "TestMultipleColumn");

        assertEquals("SELECT ID AS \"___ID\", name FROM testtable " +
                        "WHERE name IN ('test1', 'test2')",
                querySqlGenerator.getSql(query, singletonMap("name",
                        Arrays.asList("test1", "test2"))).format());
    }

    @Test
    public void testMultipleColumnLong()
    {
        Query query = meta.getQuery("testtable", "TestMultipleColumnLong");

        assertEquals("SELECT ID AS \"___ID\", name FROM testtable " +
                "WHERE ID IN (1, 2)", querySqlGenerator.getSql(query,
                singletonMap("ID", Arrays.asList("1", "2"))).format());
    }

    @Test
    public void testResolveTypeOfRefColumn()
    {
        Query query = meta.getQuery("testtable", "TestResolveRefColumn");

        assertEquals("SELECT ID AS \"___ID\", name FROM testtable " +
                "WHERE name = 'test'", querySqlGenerator.getSql(query,
                singletonMap("name", "test")).format());
    }

    @Test
    public void orderParam()
    {
        Query query = meta.createQueryFromSql("SELECT * FROM testtable " +
                "ORDER BY name <parameter:order />");
        assertEquals("SELECT * FROM testtable ORDER BY name desc",
                querySqlGenerator.getSql(query, singletonMap("order", "desc")).format());
    }

    @Test
    public void unknownColumnOrderParam()
    {
        Query query = meta.createQueryFromSql("SELECT * FROM testtable " +
                "ORDER BY unknownColumn <parameter:order />");
        assertEquals("SELECT * FROM testtable ORDER BY unknownColumn desc",
                querySqlGenerator.getSql(query, singletonMap("order", "desc")).format());
    }

    @Test
    public void testResolveTypeOfRefColumnError()
    {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Entity with alias 'public.testtable' not found, for public.testtable.name");
        Query query = meta.getQuery("testtable", "TestResolveRefColumnIllegalAE");
        querySqlGenerator.getSql(query, emptyMap());
    }

    @Test
    public void testResolveTypeOfRefColumnNPE()
    {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Can not resolve refColumn=\"testtable.unknownColumn\"");
        Query query = meta.getQuery("testtable", "TestResolveRefColumnNPE");
        querySqlGenerator.getSql(query, emptyMap());
    }

    @Test
    public void limit()
    {
        Query query = meta.createQueryFromSql("SELECT * FROM testtable " +
                "ORDER BY name <parameter:order />");
        assertEquals("SELECT * FROM testtable ORDER BY name desc LIMIT 10",
                querySqlGenerator.getSql(query, ImmutableMap.of("order", "desc", LIMIT, "10")).format());
    }
}
