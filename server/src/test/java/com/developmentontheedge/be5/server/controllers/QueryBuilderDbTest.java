package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.server.model.TablePresentation;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.server.model.table.RowModel;
import com.developmentontheedge.be5.test.ServerBe5ProjectDBTest;
import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class QueryBuilderDbTest extends ServerBe5ProjectDBTest
{
    @Inject
    private QueryBuilderController component;

    @Test
    public void executeRawQuery()
    {
        List<ResourceData> list = new ArrayList<>();
        component.executeRaw(list, "EXPLAIN ANALYZE select * from testtable");
        assertEquals(1, list.size());
        assertEquals("table", list.get(0).getType());
        String res = ((RowModel) ((TablePresentation) list.get(0).getAttributes())
                .getRows().get(0)).getCells().get(0).getContent().toString();
        assertTrue( "Wrong result = " + res, res.startsWith(
                "SELECT\n" +
                "    \"PUBLIC\".\"TESTTABLE\".\"ID\",\n" +
                "    \"PUBLIC\".\"TESTTABLE\".\"NAME\",\n" +
                "    \"PUBLIC\".\"TESTTABLE\".\"VALUE\"\n" +
                "FROM \"PUBLIC\".\"TESTTABLE\"\n" +
                "    /* PUBLIC.TESTTABLE.tableScan */\n" +
                "    /* scanCount: "));
    }

}
