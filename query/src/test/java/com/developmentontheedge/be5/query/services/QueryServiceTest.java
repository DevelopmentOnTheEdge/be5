package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryBe5ProjectDBTest;
import com.developmentontheedge.be5.query.impl.Be5QueryExecutor;
import com.developmentontheedge.beans.DynamicPropertySet;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class QueryServiceTest extends QueryBe5ProjectDBTest
{
    @Inject
    private ProjectProvider projectProvider;
    @Inject
    private DbService db;
    @Inject
    private QueryService queryService;

    private Query query;

    @Before
    public void insertOneRow()
    {
        query = projectProvider.get().getEntity("testtable").getQueries().get("All records");
        db.update("delete from testtable");
        db.insert("insert into testtable (name, value) VALUES (?, ?)",
                "testBe5QueryExecutor", "1");
    }

    @Test
    public void testExecute()
    {
        List<DynamicPropertySet> dps = queryService.build(query).execute();
        assertTrue(dps.size() > 0);

        assertEquals(String.class, dps.get(0).getProperty("name").getType());
    }

    @Test
    public void testColumnNames()
    {
        List<String> columnNames = queryService.build(query).getColumnNames();
        assertEquals(2, columnNames.size());
        assertEquals("NAME", columnNames.get(0));
    }

    @Test
    public void testCountFromQuery()
    {
        Be5QueryExecutor be5QueryExecutor = queryService.build(query);

        assertTrue(be5QueryExecutor.count() > 0);
        assertEquals("SELECT COUNT(*) AS \"count\" FROM (SELECT\n" +
                "  t.name AS \"Name\",\n" +
                "  t.value AS \"Value\"\n" +
                "FROM\n" +
                "  testtable t) AS \"data\"", be5QueryExecutor.getFinalSql().getQuery().toString());
    }

    @Test
    public void testMultipleColumn()
    {
        query = projectProvider.get().getEntity("testtable").getQueries().get("TestMultipleColumn");

        assertEquals("SELECT *\n" +
                "FROM testtable\n" +
                "WHERE name IN ('test1', 'test2') LIMIT 2147483647", queryService.
                build(query, Collections.singletonMap("name", Arrays.asList("test1", "test2"))).getFinalSql().getQuery().toString());
    }

    @Test
    public void testMultipleColumnLong()
    {
        query = projectProvider.get().getEntity("testtable").getQueries().get("TestMultipleColumnLong");

        assertEquals("SELECT *\n" +
                "FROM testtable\n" +
                "WHERE ID IN (1, 2) LIMIT 2147483647", queryService.
                build(query, Collections.singletonMap("ID", Arrays.asList("1", "2"))).getFinalSql().getQuery().toString());
    }

    @Test
    public void testResolveTypeOfRefColumn()
    {
        query = projectProvider.get().getEntity("testtable").getQueries().get("TestResolveRefColumn");

        assertEquals("SELECT *\n" +
                "FROM testtable\n" +
                "WHERE name = 'test' LIMIT 2147483647", queryService.
                build(query, Collections.singletonMap("name", "test")).getFinalSql().getQuery().toString());
    }

    @Test
    @Ignore
    public void testTestResolveRefColumnByAlias()
    {
        query = projectProvider.get().getEntity("testtable").getQueries().get("TestResolveRefColumnByAlias");

        assertEquals("SELECT *\n" +
                "FROM testtable t\n" +
                "WHERE name = 'test' LIMIT 2147483647", queryService.
                build(query, Collections.singletonMap("name", "test")).getFinalSql().getQuery().toString());
    }

    @Test
    public void testIgnoreUnknownColumn()
    {
        query = projectProvider.get().getEntity("testtable").getQueries().get("TestResolveRefColumn");
        List<DynamicPropertySet> list = queryService.build(query, Collections.singletonMap("unknownColumn", "test")).execute();
        assertEquals(list.size(), 0);
    }

    @Test(expected = Be5Exception.class)
    public void testResolveTypeOfRefColumnError()
    {
        query = projectProvider.get().getEntity("testtable").getQueries().get("TestResolveRefColumnIllegalAE");
        Be5QueryExecutor be5QueryExecutor = queryService.build(query, new HashMap<>());

        be5QueryExecutor.execute();
    }

    @Test(expected = Be5Exception.class)
    public void testResolveTypeOfRefColumnNPE()
    {
        query = projectProvider.get().getEntity("testtable").getQueries().get("TestResolveRefColumnNPE");
        Be5QueryExecutor be5QueryExecutor = queryService.build(query, new HashMap<>());

        be5QueryExecutor.execute();
        assertEquals("", be5QueryExecutor.getFinalSql().getQuery().toString());
    }
}
