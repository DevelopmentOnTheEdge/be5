package com.developmentontheedge.be5.server.services

import com.developmentontheedge.be5.base.services.Meta
import com.developmentontheedge.be5.metadata.RoleType
import com.developmentontheedge.be5.operation.services.OperationExecutor
import com.developmentontheedge.be5.server.model.TablePresentation
import com.developmentontheedge.be5.server.model.jsonapi.ErrorModel
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel
import com.developmentontheedge.be5.test.ServerTestResponse
import groovy.transform.TypeChecked
import org.junit.Before
import org.junit.Test

import javax.inject.Inject

import static junit.framework.TestCase.assertNull
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

@TypeChecked
class DocumentGeneratorTest extends TestTableQueryDBTest
{
    @Inject private Meta meta
    @Inject private DocumentGenerator documentGenerator
    @Inject private OperationExecutor operationExecutor

    @Before
    void setUp()
    {
        initGuest()
        ServerTestResponse.newMock()
    }

    @Test
    void getTablePresentation()
    {
        TablePresentation table = documentGenerator.getTablePresentation(
                meta.getQuery("testtable", "All records"), new HashMap<>())

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
        TablePresentation table = documentGenerator.getTablePresentation(
                meta.getQuery("testtable", "LinkQuick"), new HashMap<>())

        assertEquals("testtable: LinkQuick", table.getTitle())

        assertEquals("{'cells':[{" +
                "'content':'tableModelTest'," +
                "'options':{" +
                    "'link':{'url':'table/testtable/Test 1D unknown/ID=123'}," +
                    "'quick':{'visible':'true'}" +
                "}}]}", oneQuotes(jsonb.toJson(table.getRows().get(0))))
    }

    @Test
    void testTableWithFilter()
    {
        initUserWithRoles("SystemDeveloper")

        def query = meta.getQuery("testtable", "TableWithFilter")

        JsonApiModel document = documentGenerator.getJsonApiModel(query, new HashMap<>())

        assertEquals("{" +
                "'data':{'attributes':{'category':'testtable','categoryNavigation':[],'columns':['1'],'hasAggregate':false,'layout':{'topForm':'FilterByParamsInQueryOperation'},'length':1,'offset':0,'operations':[],'orderColumn':-1,'orderDir':'asc','page':'TableWithFilter','parameters':{},'rows':[{'cells':[{'content':1,'options':{}}]}],'selectable':false,'title':'testtable: TableWithFilter','totalNumberOfRows':1},'links':{'self':'table/testtable/TableWithFilter'},'type':'table'}," +
                "'included':[" +
                    "{'attributes':{" +
                        "'bean':{'values':{'_search_presets_':'','_search_':true},'meta':{'/_search_presets_':{'displayName':'_search_presets_','hidden':true,'readOnly':true,'canBeNull':true},'/_search_':{'displayName':'_search_','type':'Boolean','hidden':true,'readOnly':true,'canBeNull':true}},'order':['/_search_presets_','/_search_']}," +
                        "'entity':'testtable'," +
                        "'layout':{}," +
                        "'operation':'FilterByParamsInQueryOperation','operationParams':{}," +
                        "'operationResult':{'status':'generate'}," +
                        "'query':'TableWithFilter'," +
                        "'selectedRows':''," +
                        "'title':'FilterByParamsInQueryOperation'" +
                    "}," +
                    "'id':'topForm'," +
                    "'links':{'self':'form/testtable/TableWithFilter/FilterByParamsInQueryOperation'}," +
                    "'type':'form'}" +
                "]" +
                "}", oneQuotes(jsonb.toJson(document)))
    }

    @Test
    void testNullInSubQuery()
    {
        db.update("DELETE FROM testtableAdmin")
        db.insert("insert into testtableAdmin (name, value) VALUES (?, ?)","tableModelTest", 11)
        db.insert("insert into testtableAdmin (name, value) VALUES (?, ?)","tableModelTest", null)

        TablePresentation table = documentGenerator.getTablePresentation(
                meta.getQuery("testtableAdmin", "Test null in subQuery"), new HashMap<>())

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
    void getQueryJsonApiForUser()
    {
        JsonApiModel queryJsonApiForUser = documentGenerator.
                queryJsonApiFor("testtable", "All records", Collections.emptyMap());

        assertNotNull(queryJsonApiForUser.getData());
        assertNull(queryJsonApiForUser.getErrors());
    }

    @Test
    void accessDenied()
    {
        JsonApiModel queryJsonApiForUser = documentGenerator.
                queryJsonApiFor("testtableAdmin", "All records", Collections.emptyMap());

        assertEquals(new ErrorModel("403", "Access denied to query: testtableAdmin.All records",
                Collections.singletonMap("self", "table/testtableAdmin/All records")),
                queryJsonApiForUser.getErrors()[0]);
    }

    @Test
    void accessAllowed()
    {
        initUserWithRoles(RoleType.ROLE_SYSTEM_DEVELOPER);

        JsonApiModel queryJsonApiForUser = documentGenerator.
                queryJsonApiFor("testtableAdmin", "All records", Collections.emptyMap());

        assertNotNull(queryJsonApiForUser.getData());
        assertNull(queryJsonApiForUser.getErrors());
    }

    @Test
    void error()
    {
        JsonApiModel queryJsonApiForUser = documentGenerator.queryJsonApiFor("testtable", "Query with error", Collections.emptyMap());

        assertEquals(null, queryJsonApiForUser.getData());
        assertEquals(new ErrorModel("500", "Internal error occurred during query: testtable.Query with error",
                Collections.singletonMap("self", "table/testtable/Query with error")),
                queryJsonApiForUser.getErrors()[0]);
    }
}