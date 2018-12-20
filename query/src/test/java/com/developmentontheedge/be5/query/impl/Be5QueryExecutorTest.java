package com.developmentontheedge.be5.query.impl;

import com.developmentontheedge.be5.query.QueryBe5ProjectDBTest;
import com.developmentontheedge.be5.query.services.QueryExecutor;
import org.junit.Test;

import javax.inject.Inject;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.*;

public class Be5QueryExecutorTest extends QueryBe5ProjectDBTest
{
    @Inject
    private QueryExecutor queryService;

    @Test
    public void testCountFromQuery()
    {
        Be5QueryExecutor be5QueryExecutor = queryService.build(meta.getQuery("testtable", "All records"), emptyMap());

        assertEquals("SELECT COUNT(*) AS \"count\" FROM (SELECT\n" +
                "  t.name AS \"Name\",\n" +
                "  t.value AS \"Value\"\n" +
                "FROM\n" +
                "  testtable t) AS \"data\"", be5QueryExecutor.getFinalSql(Be5QueryExecutor.ExecuteType.COUNT).getQuery().toString());
    }
}
