package com.developmentontheedge.be5.components.impl.model;

import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.test.AbstractProjectTestH2DB;
import com.developmentontheedge.beans.DynamicPropertySet;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class Be5QueryExecutorTest extends AbstractProjectTestH2DB
{
    private Query query = sp.getProject().getEntity("testtable").getQueries().get("All records");

    @BeforeClass
    public static void hasOneRow()
    {
        SqlService db = sp.getSqlService();
        if((Long)db.selectScalar("select count(*) from testtable") == 0)
        {
            db.insert("insert into testtable (name, value) VALUES (?, ?)",
                    "test", "1");
        }
    }

    @Test
    public void testExecute()
    {
        Be5QueryExecutor be5QueryExecutor = new Be5QueryExecutor(query, new HashMap<>(), mock(Request.class), sp);
        List<DynamicPropertySet> dps = be5QueryExecutor.execute().toList();
        assertTrue(dps.size() > 0);

        Class<?> klass = dps.get(0).getProperty("name").getType();
        assertEquals(String.class, klass);
    }

    @Test
    public void testColumnNames()
    {
        Be5QueryExecutor be5QueryExecutor = new Be5QueryExecutor(query, new HashMap<>(), mock(Request.class), sp);
        List<String> columnNames = be5QueryExecutor.getColumnNames();
        assertEquals(2, columnNames.size());
        assertEquals("NAME", columnNames.get(0));
    }

    @Test
    public void testCountFromQuery()
    {
        Be5QueryExecutor be5QueryExecutor = new Be5QueryExecutor(query, new HashMap<>(), mock(Request.class), sp);

        assertTrue(be5QueryExecutor.count() > 0);
        assertEquals("SELECT COUNT(*) AS \"count\" FROM " + "(" +
                "SELECT\n" +
                "      t.name AS \"Name\",\n" +
                "      t.value AS \"Value\"\n" +
                "    FROM\n" +
                "      testtable t" +
                ") " +"AS \"data\"", be5QueryExecutor.getFinalSql());
    }
}
