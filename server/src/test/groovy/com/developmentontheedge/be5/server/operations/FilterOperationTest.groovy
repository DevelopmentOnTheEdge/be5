package com.developmentontheedge.be5.server.operations

import com.developmentontheedge.be5.operation.model.OperationResult
import com.developmentontheedge.be5.operation.util.Either
import com.developmentontheedge.be5.server.model.FrontendAction
import com.developmentontheedge.be5.server.model.TablePresentation
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel
import com.developmentontheedge.be5.test.SqlMockOperationTest
import com.developmentontheedge.beans.json.JsonFactory
import org.junit.Test

import static com.developmentontheedge.be5.base.FrontendConstants.SEARCH_PARAM
import static com.developmentontheedge.be5.base.FrontendConstants.SEARCH_PRESETS_PARAM
import static com.developmentontheedge.be5.server.FrontendActions.CLOSE_MAIN_MODAL
import static com.developmentontheedge.be5.server.FrontendActions.UPDATE_PARENT_DOCUMENT
import static org.junit.Assert.assertEquals

class FilterOperationTest extends SqlMockOperationTest
{
    @Test
    void generate()
    {
        def operation = createOperation("testtable", "All records", "Filter", "")
        Either<Object, OperationResult> generate = generateOperation(operation, [:])

        assertEquals("{" +
                "'values':{'name':'','value':'','_search_presets_':'','_search_':true}," +
                "'meta':{" +
                "'/name':{'displayName':'name','canBeNull':true,'columnSize':'20'}," +
                "'/value':{'displayName':'value','canBeNull':true,'columnSize':'30'}," +
                "'/_search_presets_':{'displayName':'_search_presets_','hidden':true,'readOnly':true,'canBeNull':true}," +
                "'/_search_':{'displayName':'_search_','type':'Boolean','hidden':true,'readOnly':true,'canBeNull':true}}," +
                "'order':['/name','/value','/_search_presets_','/_search_']" +
                "}", oneQuotes(JsonFactory.bean(generate.getFirst())))
    }

    @Test
    void generateWithOperationParams()
    {
        def operation = createOperation("testtable", "All records", "Filter", [name: "b", (SEARCH_PARAM): "true", (SEARCH_PRESETS_PARAM): "name"])
        Either<Object, OperationResult> generate = generateOperation(operation, "")

        assertEquals("{" +
                "'values':{'name':'b','value':'','_search_presets_':'name','_search_':true}," +
                "'meta':{" +
                "'/name':{'displayName':'name','readOnly':true,'canBeNull':true,'columnSize':'20'}," +
                "'/value':{'displayName':'value','canBeNull':true,'columnSize':'30'}," +
                "'/_search_presets_':{'displayName':'_search_presets_','hidden':true,'readOnly':true,'canBeNull':true}," +
                "'/_search_':{'displayName':'_search_','type':'Boolean','hidden':true,'readOnly':true,'canBeNull':true}}," +
                "'order':['/name','/value','/_search_presets_','/_search_']" +
                "}", oneQuotes(JsonFactory.bean(generate.getFirst())))
    }

    @Test
    void generateDocumentForm()
    {
        def operation = createOperation("testtable", "All records", "Filter",
                [_search_:true, _search_presets_:"", name: "name1"])
        Either<Object, OperationResult> generate = generateOperation(operation, [:])

        assertEquals(
                "{'name':'name1','value':'','_search_presets_':'','_search_':true}"
                , oneQuotes(JsonFactory.beanValues(generate.getFirst())))
    }

    @Test
    void executeFilterWithoutOperationParamProperty()
    {
        def operation = createOperation("testtable", "TestFilterByParamsInQueryOperation",
                "FilterByParamsInQueryOperation", [value: "value1"])
        Either<Object, OperationResult> execute = executeOperation(operation, [:])

        assertEquals("finished", oneQuotes(execute.getSecond().getStatus()))
        def details = (FrontendAction[]) execute.getSecond().getDetails()
        assertEquals("[_search_presets_:value, _search_:true, value:value1]",
                oneQuotes(((TablePresentation) ((JsonApiModel) details[0].getValue())
                        .getData().getAttributes()).getParameters().toString()))
    }

    @Test
    void executeFilterWithoutOperationParamProperty2()
    {
        def operation = createOperation("testtable", "TestFilterByParamsInQueryOperation",
                "FilterByParamsInQueryOperation", [value: "value1"])
        Either<Object, OperationResult> execute = executeOperation(operation, [_search_:true, _search_presets_:"value", name: "name1"])

        assertEquals("finished", oneQuotes(execute.getSecond().getStatus()))
        def details = (FrontendAction[]) execute.getSecond().getDetails()
        assertEquals("[name:name1, _search_presets_:value, _search_:true, value:value1]",
                oneQuotes(((TablePresentation) ((JsonApiModel) details[0].getValue())
                        .getData().getAttributes()).getParameters().toString()))
    }

    @Test
    void executeFilterWithoutOperationParamProperty3()
    {
        def operation = createOperation("testtable", "TestFilterByParamsInQueryOperation",
                "FilterByParamsInQueryOperation", [value: "value1"])
        Either<Object, OperationResult> execute = executeOperation(operation, [name: "name1"])

        assertEquals("finished", oneQuotes(execute.getSecond().getStatus()))
        def details = (FrontendAction[]) execute.getSecond().getDetails()
        assertEquals("[name:name1, _search_presets_:value, _search_:true, value:value1]",
                oneQuotes(((TablePresentation) ((JsonApiModel) details[0].getValue())
                        .getData().getAttributes()).getParameters().toString()))
    }

    @Test
    void changeFilterTest()
    {
        def operation = createOperation("testtable", "TestFilterByParamsInQueryOperation",
                "FilterByParamsInQueryOperation", [_search_:true, _search_presets_:"", name: "name1"])
        Either<Object, OperationResult> execute = executeOperation(operation, [_search_:true, _search_presets_:"", name: "name2"])

        assertEquals("finished", oneQuotes(execute.getSecond().getStatus()))
        def details = (FrontendAction[]) execute.getSecond().getDetails()
        assertEquals("[name:name2, _search_presets_:, _search_:true]",
                oneQuotes(((TablePresentation) ((JsonApiModel) details[0].getValue())
                        .getData().getAttributes()).getParameters().toString()))
    }

    @Test
    void execute()
    {
        def operation = createOperation("testtable", "All records", "Filter", [name: "test"])
        Either<Object, OperationResult> execute = executeOperation(operation, [:])

        assertEquals("finished",
                oneQuotes(execute.getSecond().getStatus()))

        def details = (FrontendAction[]) execute.getSecond().getDetails()

        assertEquals("[name:test, _search_presets_:name, _search_:true]",
                oneQuotes(((TablePresentation) ((JsonApiModel) details[0].getValue())
                        .getData().getAttributes()).getParameters().toString()))

        assertEquals(UPDATE_PARENT_DOCUMENT, details[0].getType())
        assertEquals(CLOSE_MAIN_MODAL, details[1].getType())
    }

    @Test
    void executeOldRedirectFilter()
    {
        def operation = createOperation("testtable", "All records", "OldRedirectFilter", [name: "test"])
        Either<Object, OperationResult> execute = executeOperation(operation, [:])

        assertEquals("redirect",
                oneQuotes(execute.getSecond().getStatus()))

        assertEquals("table/testtable/All records/name=test",
                (String) execute.getSecond().getDetails())
    }

}
