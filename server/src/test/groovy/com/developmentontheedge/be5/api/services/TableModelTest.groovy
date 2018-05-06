package com.developmentontheedge.be5.api.services

import javax.inject.Inject
import com.developmentontheedge.be5.metadata.model.Query
import com.developmentontheedge.be5.testutils.TestTableQueryDBTest
import org.junit.Test

import static org.junit.Assert.*


class TableModelTest extends TestTableQueryDBTest
{
    @Inject TableModelService tableModelService

    @Test
    void testExecuteSubQuery()
    {
        Query query = projectProvider.getProject().getEntity("testtable").getQueries().get("Sub Query")
        def tableModel = tableModelService.builder(query, new HashMap<>())
                .limit(20)
                .build()

        assertEquals("{'content':'user1<br/> user2','options':{}}",
                oneQuotes(jsonb.toJson(tableModel.getRows().get(0).getCells().get(2))))
    }

}