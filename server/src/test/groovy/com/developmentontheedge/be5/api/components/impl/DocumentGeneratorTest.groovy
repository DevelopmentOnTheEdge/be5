package com.developmentontheedge.be5.api.components.impl

import com.developmentontheedge.be5.api.Request
import com.developmentontheedge.be5.api.Response
import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.components.RestApiConstants
import com.developmentontheedge.be5.components.impl.DocumentGenerator
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.model.TablePresentation
import com.developmentontheedge.be5.testutils.TestTableQueryDBTest
import com.google.common.collect.ImmutableMap
import org.junit.Test


import static org.junit.Assert.*
import static org.mockito.Mockito.mock

class DocumentGeneratorTest extends TestTableQueryDBTest
{
    @Inject private Meta meta

    private Response response = mock(Response.class)
    private Request request = getSpyMockRecForQuery("testtable", "All records", "")

    @Test
    void getTablePresentation()
    {
        TablePresentation testtable = new DocumentGenerator(request, response, injector).getTablePresentation(
                meta.getQuery("testtable", "All records", Collections.singletonList("Guest")), new HashMap<>())

        assertEquals("testtable: All records", testtable.getTitle())

        assertEquals("[{'cells':[" +
                "{'content':'tableModelTest','options':{}}," +
                "{'content':'1','options':{}}" +
            "]}]",  oneQuotes(jsonb.toJson(testtable.getRows())))
    }

    @Test
    void testLinkQuick()
    {
        TablePresentation testtable = new DocumentGenerator(request, response, injector).getTablePresentation(
                meta.getQuery("testtable", "LinkQuick", Collections.singletonList("SystemDeveloper")), new HashMap<>())

        assertEquals("testtable: LinkQuick", testtable.getTitle())

        assertEquals("{'cells':[{" +
                "'content':'tableModelTest'," +
                "'options':{" +
                    "'link':{'url':'table/testtable/Test 1D unknown/ID=1'}," +
                    "'quick':{'visible':'true'}" +
                "}}]}", oneQuotes(jsonb.toJson(testtable.getRows().get(0))))
    }

    @Test
    void testNullInSubQuery()
    {
        //todo add test and fix for numeric types, SELECT name FROM testtableAdmin t WHERE t.value = '11'

        db.update("DELETE FROM testtableAdmin")
        db.insert("insert into testtableAdmin (name, value) VALUES (?, ?)","tableModelTest", 11)
        db.insert("insert into testtableAdmin (name, value) VALUES (?, ?)","tableModelTest", null)

        TablePresentation testtable = new DocumentGenerator(request, response, injector).getTablePresentation(
                meta.getQuery("testtableAdmin", "Test null in subQuery",
                        Collections.singletonList("SystemDeveloper")), new HashMap<>())

        assertEquals("[" +
                "{'cells':[" +
                    "{'content':'tableModelTest','options':{}}," +
                    "{'content':11,'options':{}}," +
                    "{'content':'tableModelTest','options':{'sql':{}}}]}," +
                "{'cells':[" +
                    "{'content':'tableModelTest','options':{}}," +
                    "{'options':{}}," +
                    "{'content':'','options':{'sql':{}}}" +
                "]}]", oneQuotes(jsonb.toJson(testtable.getRows())))
    }
}