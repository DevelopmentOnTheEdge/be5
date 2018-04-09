package com.developmentontheedge.be5.api.services

import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.metadata.RoleType
import com.developmentontheedge.be5.model.TablePresentation
import com.developmentontheedge.be5.model.jsonapi.JsonApiModel
import com.developmentontheedge.be5.testutils.TestTableQueryDBTest
import groovy.transform.TypeChecked
import org.junit.Test

import static org.junit.Assert.assertEquals

@TypeChecked
class DocumentGeneratorTest extends TestTableQueryDBTest
{
    @Inject private Meta meta
    @Inject private DocumentGenerator documentGenerator
    @Inject private OperationExecutor operationExecutor

    @Test
    void getTablePresentation()
    {
        TablePresentation table = documentGenerator.getTablePresentation(
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
        TablePresentation table = documentGenerator.getTablePresentation(
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
    void testTableWithFilter()
    {
        initUserWithRoles("SystemDeveloper")

        def query = meta.getQuery("testtable", "TableWithFilter", Collections.singletonList("SystemDeveloper"))

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
    void groovyTableTest()
    {
        def table = (TablePresentation)documentGenerator.
                getTablePresentation(meta.getQueryIgnoringRoles("testtableAdmin", "TestGroovyTable"), new HashMap<>())

        assertEquals "['name','value']", oneQuotes(jsonb.toJson(table.getColumns()))

        assertEquals("[" +
                "{'cells':[{'content':'a1','options':{}},{'content':'b1','options':{}}]}," +
                "{'cells':[{'content':'a2','options':{}},{'content':'b2','options':{}}]}]"
                , oneQuotes(jsonb.toJson(table.getRows())))
    }

    @Test
    void generateForm()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER)

        def result = documentGenerator.generateForm(
                operationExecutor.create("testtable", "All records", "Insert", [] as String[], [:]),
                [name: "test1", value: "2"])

        assertEquals("{'bean':{'values':{'name':'test1','value':'2'},'meta':{'/name':{'displayName':'name','columnSize':'20'},'/value':{'displayName':'value','columnSize':'30'}},'order':['/name','/value']}," +
            "'entity':'testtable','layout':{},'operation':'Insert','operationParams':{},'operationResult':{'status':'generate'},'query':'All records','selectedRows':'','title':'Добавить'}",
                oneQuotes(jsonb.toJson(result.getFirst())))
    }
}