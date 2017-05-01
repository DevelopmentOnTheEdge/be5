package com.developmentontheedge.be5.components.impl.model;

import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.beans.DynamicPropertySet;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class Be5QueryExecutorTest extends AbstractProjectTest
{
    private static Be5QueryExecutor be5QueryExecutor;

    @Before
    public void init(){
        Query query = sp.getProject().getEntity("testtable").getQueries().get("All records");
        Request req = mock(Request.class);
        be5QueryExecutor = new Be5QueryExecutor(query, new HashMap<>(), req, sp);
    }

    @Test
    public void testExecute()
    {
        List<DynamicPropertySet> dps = be5QueryExecutor.execute().toList();
        assertEquals(2, dps.size());

        Class<?> klass = dps.get(0).getProperty("name").getType();
        assertEquals(String.class, klass);
        assertEquals("test", klass.cast(dps.get(0).getProperty("name").getValue()));
    }

    @Test
    public void testColumnNames()
    {
        List<String> columnNames = be5QueryExecutor.getColumnNames();
        assertEquals(2, columnNames.size());
        assertEquals("NAME", columnNames.get(0));
    }

    @Test
    public void testCountFromQuery()
    {
        long count = be5QueryExecutor.count();
        assertEquals(2, count);
        assertEquals("SELECT COUNT(*) AS \"count\" FROM " + "(" +
                "SELECT\n" +
                "      t.name AS \"Name\",\n" +
                "      t.value AS \"Value\"\n" +
                "    FROM\n" +
                "      testtable t" +
                ") " +"AS \"data\"", be5QueryExecutor.getFinalSql());
    }
}
