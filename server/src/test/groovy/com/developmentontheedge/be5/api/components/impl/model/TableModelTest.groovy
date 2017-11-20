package com.developmentontheedge.be5.api.components.impl.model

import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.metadata.model.Query
import com.developmentontheedge.be5.testutils.TestTableQueryDBTest
import org.junit.Test

import static org.junit.Assert.*


class TableModelTest extends TestTableQueryDBTest
{
    @Test
    void testExecuteSubQuery()
    {
        Query query = projectProvider.getProject().getEntity("testtable").getQueries().get("Sub Query")
        TableModel table = TableModel
                .from(query, new HashMap<>(), false, injector)
                .limit(20)
                .build()

        assertEquals("{'content':'user1<br/> user2','options':{}}",
                oneQuotes(jsonb.toJson(table.getRows().get(0).getCells().get(2))))
    }


}