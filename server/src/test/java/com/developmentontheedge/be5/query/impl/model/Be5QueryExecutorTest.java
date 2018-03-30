package com.developmentontheedge.be5.query.impl.model;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.test.Be5ProjectDBTest;
import com.developmentontheedge.beans.DynamicPropertySet;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class Be5QueryExecutorTest extends Be5ProjectDBTest
{
    @Inject private ProjectProvider projectProvider;
    @Inject private SqlService db;
    @Inject private Injector injector;

    private Query query;

    @Before
    public void hasOneRow()
    {
        query = projectProvider.getProject().getEntity("testtable").getQueries().get("All records");
        db.update("delete from testtable");
        db.insert("insert into testtable (name, value) VALUES (?, ?)",
                "testBe5QueryExecutor", "1");
    }

    @Test
    public void testExecute()
    {
        Be5QueryExecutor be5QueryExecutor = new Be5QueryExecutor(query, new HashMap<>(), injector);
        List<DynamicPropertySet> dps = be5QueryExecutor.execute();
        assertTrue(dps.size() > 0);

        assertEquals(String.class, dps.get(0).getProperty("name").getType());
    }

    @Test
    public void testColumnNames()
    {
        Be5QueryExecutor be5QueryExecutor = new Be5QueryExecutor(query, new HashMap<>(), injector);
        List<String> columnNames = be5QueryExecutor.getColumnNames();
        assertEquals(2, columnNames.size());
        assertEquals("NAME", columnNames.get(0));
    }

    @Test
    public void testCountFromQuery()
    {
        Be5QueryExecutor be5QueryExecutor = new Be5QueryExecutor(query, new HashMap<>(), injector);

        assertTrue(be5QueryExecutor.count() > 0);
        assertEquals("SELECT COUNT(*) AS \"count\" FROM (SELECT\n" +
                "  t.name AS \"Name\",\n" +
                "  t.value AS \"Value\"\n" +
                "FROM\n" +
                "  testtable t) AS \"data\"", be5QueryExecutor.getFinalSql());
    }

    @Test
    public void testResolveTypeOfRefColumn()
    {
        query = projectProvider.getProject().getEntity("testtable").getQueries().get("TestResolveRefColumn");
        Be5QueryExecutor be5QueryExecutor = new Be5QueryExecutor(query, Collections.singletonMap("name", "test"), injector);

        be5QueryExecutor.execute();

        assertEquals("SELECT *\n" +
                "FROM testtable\n" +
                "WHERE name = 'test' LIMIT 2147483647", be5QueryExecutor.getFinalSql());
    }

    @Test(expected = RuntimeException.class)
    public void testResolveUnknownColumn()
    {
        query = projectProvider.getProject().getEntity("testtable").getQueries().get("TestResolveRefColumn");
        Be5QueryExecutor be5QueryExecutor = new Be5QueryExecutor(query, Collections.singletonMap("unknownColumn", "test"), injector);

        be5QueryExecutor.execute();
    }

    @Test(expected = Be5Exception.class)
    public void testResolveTypeOfRefColumnError()
    {
        query = projectProvider.getProject().getEntity("testtable").getQueries().get("TestResolveRefColumnIllegalAE");
        Be5QueryExecutor be5QueryExecutor = new Be5QueryExecutor(query, new HashMap<>(), injector);

        be5QueryExecutor.execute();
    }

    @Test(expected = NullPointerException.class)
    public void testResolveTypeOfRefColumnNPE()
    {
        query = projectProvider.getProject().getEntity("testtable").getQueries().get("TestResolveRefColumnNPE");
        Be5QueryExecutor be5QueryExecutor = new Be5QueryExecutor(query, new HashMap<>(), injector);

        be5QueryExecutor.execute();
        assertEquals("", be5QueryExecutor.getFinalSql());
    }
}
