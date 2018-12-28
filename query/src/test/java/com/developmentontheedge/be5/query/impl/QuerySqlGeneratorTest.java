package com.developmentontheedge.be5.query.impl;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryBe5ProjectDBTest;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.inject.Inject;
import java.util.Arrays;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

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
                        "WHERE name IN ('test1', 'test2') LIMIT 2147483647",
                querySqlGenerator.getSql(query, singletonMap("name",
                        Arrays.asList("test1", "test2"))).format());
    }

    @Test
    public void testMultipleColumnLong()
    {
        Query query = meta.getQuery("testtable", "TestMultipleColumnLong");

        assertEquals("SELECT ID AS \"___ID\", name FROM testtable " +
                "WHERE ID IN (1, 2) LIMIT 2147483647", querySqlGenerator.getSql(query,
                singletonMap("ID", Arrays.asList("1", "2"))).format());
    }

    @Test
    public void testResolveTypeOfRefColumn()
    {
        Query query = meta.getQuery("testtable", "TestResolveRefColumn");

        assertEquals("SELECT ID AS \"___ID\", name FROM testtable " +
                "WHERE name = 'test' LIMIT 2147483647", querySqlGenerator.getSql(query,
                singletonMap("name", "test")).format());
    }

    @Test
    @Ignore
    public void testTestResolveRefColumnByAlias()
    {
        Query query = meta.getQuery("testtable", "TestResolveRefColumnByAlias");

        assertEquals("SELECT *\n" +
                "FROM testtable t\n" +
                "WHERE name = 'test' LIMIT 2147483647", querySqlGenerator.
                getSql(query, singletonMap("name", "test")).format());
    }

    @Test
    @Ignore
    public void orderParam()
    {
        Query query = meta.createQueryFromSql("SELECT * FROM testtable " +
                "ORDER BY name <parameter:order />");
        assertEquals("SELECT * FROM testtable ORDER BY name desc LIMIT 2147483647",
                querySqlGenerator.getSql(query, singletonMap("order", "desc")).format());
    }

    @Test
    public void unknownColumnOrderParam()
    {
        Query query = meta.createQueryFromSql("SELECT * FROM testtable " +
                "ORDER BY unknownColumn <parameter:order />");
        assertEquals("SELECT * FROM testtable ORDER BY unknownColumn desc LIMIT 2147483647",
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
    public void testCountFromQuery()
    {
        String sql = querySqlGenerator.getSql(meta.getQuery("testtable", "All records"), emptyMap(),
                Be5SqlQueryExecutor.ExecuteType.COUNT).format();

        assertEquals("SELECT COUNT(*) AS \"count\" FROM (SELECT\n" +
                "  t.name AS \"Name\",\n" +
                "  t.value AS \"Value\"\n" +
                "FROM\n" +
                "  testtable t) AS \"data\"", sql);
    }
}
