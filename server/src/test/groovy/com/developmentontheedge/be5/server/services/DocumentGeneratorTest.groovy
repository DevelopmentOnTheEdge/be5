package com.developmentontheedge.be5.server.services

import com.developmentontheedge.be5.base.services.Meta
import com.developmentontheedge.be5.operation.model.OperationInfo
import com.developmentontheedge.be5.operation.model.OperationResult
import com.developmentontheedge.be5.operation.model.OperationStatus
import com.developmentontheedge.be5.operation.services.OperationExecutor
import com.developmentontheedge.be5.operation.util.Either
import com.developmentontheedge.be5.server.model.FormPresentation
import com.developmentontheedge.be5.server.model.TablePresentation
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel
import com.developmentontheedge.beans.json.JsonFactory
import groovy.transform.TypeChecked
import org.junit.Before
import org.junit.Test

import javax.inject.Inject

import static org.junit.Assert.assertEquals


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
    void generateForm()
    {
        def result = documentGenerator.generateForm(
                operationExecutor.create(new OperationInfo(meta.getOperation("testtable", "Insert"))
                        , "All records", [] as String[], [:]),
                [name: "test1", value: "2"])

        assertEquals("{'bean':{'values':{'name':'test1','value':'2'},'meta':{'/name':{'displayName':'name','columnSize':'20'},'/value':{'displayName':'value','columnSize':'30'}},'order':['/name','/value']}," +
            "'entity':'testtable','layout':{},'operation':'Insert','operationParams':{},'operationResult':{'status':'generate'},'query':'All records','selectedRows':'','title':'Добавить'}",
                oneQuotes(jsonb.toJson(result.getFirst())))
    }

    @Test
    void executeWithGenerateErrorInProperty()
    {
        def operation = createOperation("testtableAdmin", "All records", "ErrorProcessing", "")

        Either<FormPresentation, OperationResult> either = documentGenerator
                .executeForm(operation, ['name': 'generateErrorInProperty'])

        assertEquals "{'displayName':'name','columnSize':'30','status':'error','message':'Error in property (getParameters)'}",
                oneQuotes(either.getFirst().getBean().getJsonObject("meta").getJsonObject("/name").toString())

        assertEquals OperationStatus.ERROR, operation.getResult().getStatus()
        assertEquals "Error in property (getParameters)",// - [ name: 'name', type: class java.lang.String, value: generateErrorInProperty (String) ]",
                operation.getResult().getMessage()
    }
}