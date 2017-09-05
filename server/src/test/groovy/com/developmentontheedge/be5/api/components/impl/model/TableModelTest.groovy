package com.developmentontheedge.be5.api.components.impl.model

import com.developmentontheedge.be5.api.Request
import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.metadata.model.Query
import com.developmentontheedge.be5.testutils.TestTableQueryDBTest
import org.junit.Test

import static org.junit.Assert.*
import static org.mockito.Mockito.mock


class TableModelTest extends TestTableQueryDBTest
{
    @Test
    void testExecuteSubQuery() {
        Query query = projectProvider.getProject().getEntity("testtable").getQueries().get("Sub Query")
        TableModel table = TableModel
                .from(query, new HashMap<>(), mock(Request.class), false, injector)
                .limit(20)
                .build()

        assertEquals("user1; user2", table.getRows().get(0).getCells().get(2).content)
    }


}