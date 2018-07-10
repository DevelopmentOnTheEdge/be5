package com.developmentontheedge.sql;

import com.developmentontheedge.sql.format.BasicQueryContext;
import com.developmentontheedge.sql.format.ContextApplier;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;


public class ContextApplierApplySessionVarsTest
{
    @Test
    public void listString()
    {
        AstStart start = SqlQuery.parse("SELECT * FROM table WHERE name IN (<session:projects multiple=\"true\"/>)");

        ContextApplier contextApplier = new ContextApplier(new BasicQueryContext.Builder()
                .sessionVar("projects", Arrays.asList("Demo", "Test project"))
                .build());
        contextApplier.applyContext(start);

        assertEquals("SELECT * FROM table WHERE name IN ('Demo', 'Test project')", start.getQuery().toString());
    }

    @Test
    public void list()
    {
        AstStart start = SqlQuery.parse("SELECT * FROM table WHERE value IN (<session:projects multiple=\"true\"/>)");

        ContextApplier contextApplier = new ContextApplier(new BasicQueryContext.Builder()
                .sessionVar("projects", Arrays.asList(1, 2))
                .build());
        contextApplier.applyContext(start);

        assertEquals("SELECT * FROM table WHERE value IN (1, 2)", start.getQuery().toString());
    }

    @Test
    public void array()
    {
        AstStart start = SqlQuery.parse("SELECT * FROM table WHERE name IN (<session:projects multiple=\"true\"/>)");

        ContextApplier contextApplier = new ContextApplier(new BasicQueryContext.Builder()
                .sessionVar("projects", new String[]{"Demo", "Test project"})
                .build());
        contextApplier.applyContext(start);

        assertEquals("SELECT * FROM table WHERE name IN ('Demo', 'Test project')", start.getQuery().toString());
    }
}