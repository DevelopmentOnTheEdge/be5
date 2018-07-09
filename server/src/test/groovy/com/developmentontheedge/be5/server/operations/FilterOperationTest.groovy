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
        Either<Object, OperationResult> generate = generateOperation(
                "testtable", "All records", "Filter", "", [name: "b", (SEARCH_PARAM): "true", (SEARCH_PRESETS_PARAM): "name"])

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
    void execute()
    {
        Either<Object, OperationResult> execute = executeOperation(
                "testtable", "All records", "Filter", "", [name: "test"])

        assertEquals("finished",
                oneQuotes(execute.getSecond().getStatus()))

        def details = (FrontendAction) execute.getSecond().getDetails()

        assertEquals("[_search_presets_:name, name:test, _search_:true]",
                oneQuotes(((TablePresentation) ((JsonApiModel) details.getValue())
                        .getData().getAttributes()).getParameters().toString()))

        assertEquals(UPDATE_PARENT_DOCUMENT, details.getType())
    }

    @Test
    void executeOldRedirectFilter()
    {
        Either<Object, OperationResult> execute = executeOperation(
                "testtable", "All records", "OldRedirectFilter", "", [name: "test"])

        assertEquals("redirect",
                oneQuotes(execute.getSecond().getStatus()))

        assertEquals("table/testtable/All records/_search_presets_=name/name=test/_search_=true",
                (String) execute.getSecond().getDetails())
    }

}