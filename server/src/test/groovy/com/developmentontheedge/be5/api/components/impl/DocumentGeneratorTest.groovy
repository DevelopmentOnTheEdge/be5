package com.developmentontheedge.be5.api.components.impl

import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.components.DocumentGenerator
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.model.TablePresentation
import com.developmentontheedge.be5.testutils.TestTableQueryDBTest
import groovy.transform.TypeChecked
import org.junit.Test


import static org.junit.Assert.*


@TypeChecked
class DocumentGeneratorTest extends TestTableQueryDBTest
{
    @Inject private Meta meta
    @Inject private DocumentGenerator documentGenerator

    @Test
    void getTablePresentation()
    {
        TablePresentation table = documentGenerator.getTable(
                meta.getQuery("testtable", "All records", Collections.singletonList("Guest")), new HashMap<>())

        assertEquals("testtable: All records", table.getTitle())

        assertEquals "['Name','Value']", oneQuotes(jsonb.toJson(table.getColumns()))

        assertEquals("[{'cells':[" +
                "{'content':'tableModelTest','options':{}}," +
                "{'content':'1','options':{}}" +
            "]}]",  oneQuotes(jsonb.toJson(table.getRows())))
    }

    @Test
    void testLinkQuick()
    {
        TablePresentation table = documentGenerator.getTable(
                meta.getQuery("testtable", "LinkQuick", Collections.singletonList("SystemDeveloper")), new HashMap<>())

        assertEquals("testtable: LinkQuick", table.getTitle())

        assertEquals("{'cells':[{" +
                "'content':'tableModelTest'," +
                "'options':{" +
                    "'link':{'url':'table/testtable/Test 1D unknown/ID=123'}," +
                    "'quick':{'visible':'true'}" +
                "}}]}", oneQuotes(jsonb.toJson(table.getRows().get(0))))
    }

    @Test
    void testNullInSubQuery()
    {
        db.update("DELETE FROM testtableAdmin")
        db.insert("insert into testtableAdmin (name, value) VALUES (?, ?)","tableModelTest", 11)
        db.insert("insert into testtableAdmin (name, value) VALUES (?, ?)","tableModelTest", null)

        TablePresentation table = documentGenerator.getTable(
                meta.getQuery("testtableAdmin", "Test null in subQuery",
                        Collections.singletonList("SystemDeveloper")), new HashMap<>())

        assertEquals("[" +
                "{'cells':[" +
                    "{'content':'tableModelTest','options':{}}," +
                    "{'content':11,'options':{}}," +
                    "{'content':'tableModelTest','options':{}}]}," +
                "{'cells':[" +
                    "{'content':'tableModelTest','options':{}}," +
                    "{'options':{}}," +
                    "{'content':'','options':{}}" +
                "]}]", oneQuotes(jsonb.toJson(table.getRows())))
    }

    @Test
    void groovyTableTest() throws Exception
    {
        def table = (TablePresentation)documentGenerator.
                routeAndRun(meta.getQueryIgnoringRoles("testtableAdmin", "TestGroovyTable"), new HashMap<>())

        assertEquals "['name','value']", oneQuotes(jsonb.toJson(table.getColumns()))

        assertEquals("[" +
                "{'cells':[{'content':'a1','options':{}},{'content':'b1','options':{}}]}," +
                "{'cells':[{'content':'a2','options':{}},{'content':'b2','options':{}}]}]"
                , oneQuotes(jsonb.toJson(table.getRows())))
    }
}