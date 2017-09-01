package com.developmentontheedge.be5.components.impl.model;

import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.test.AbstractProjectIntegrationH2Test;
import com.developmentontheedge.beans.DynamicPropertySet;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class Be5QueryExecutorTest extends AbstractProjectIntegrationH2Test
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
        Be5QueryExecutor be5QueryExecutor = new Be5QueryExecutor(query, new HashMap<>(), mock(Request.class), injector);
        List<DynamicPropertySet> dps = be5QueryExecutor.execute();
        assertTrue(dps.size() > 0);

        assertEquals(String.class, dps.get(0).getProperty("name").getType());
    }

    @Test
    public void testColumnNames()
    {
        Be5QueryExecutor be5QueryExecutor = new Be5QueryExecutor(query, new HashMap<>(), mock(Request.class), injector);
        List<String> columnNames = be5QueryExecutor.getColumnNames();
        assertEquals(2, columnNames.size());
        assertEquals("NAME", columnNames.get(0));
    }

    @Test
    public void testCountFromQuery()
    {
        Be5QueryExecutor be5QueryExecutor = new Be5QueryExecutor(query, new HashMap<>(), mock(Request.class), injector);

        assertTrue(be5QueryExecutor.count() > 0);
        assertEquals("SELECT COUNT(*) AS \"count\" FROM (SELECT\n" +
                "  t.name AS \"Name\",\n" +
                "  t.value AS \"Value\"\n" +
                "FROM\n" +
                "  testtable t) AS \"data\"", be5QueryExecutor.getFinalSql());
    }
}
