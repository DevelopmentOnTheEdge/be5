package com.developmentontheedge.be5.operations

import com.developmentontheedge.be5.model.TablePresentation
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.test.SqlMockOperationTest
import com.developmentontheedge.be5.util.Either
import com.developmentontheedge.beans.json.JsonFactory
import org.junit.Test

import static com.developmentontheedge.be5.components.FrontendConstants.SEARCH_PARAM
import static com.developmentontheedge.be5.components.FrontendConstants.SEARCH_PRESETS_PARAM
import static org.junit.Assert.assertEquals


class FilterOperationTest extends SqlMockOperationTest
{
    @Test
    void generate()
    {
        def operation = getOperation("testtableAdmin", "All records", "Filter", "")
        Either<Object, OperationResult> generate = generateOperation(operation, [:])

        assertEquals("[type:modal]", operation.getLayout().toString())

        assertEquals("{" +
            "'values':{'name':'','value':'','_search_presets_':'','_search_':true}," +
            "'meta':{" +
                "'/name':{'displayName':'name','canBeNull':true}," +
                "'/value':{'displayName':'value','type':'Integer','canBeNull':true}," +
                "'/_search_presets_':{'displayName':'_search_presets_','hidden':true,'readOnly':true,'canBeNull':true}," +
                "'/_search_':{'displayName':'_search_','type':'Boolean','hidden':true,'readOnly':true,'canBeNull':true}}," +
            "'order':['/name','/value','/_search_presets_','/_search_']" +
        "}", oneQuotes(JsonFactory.bean(generate.getFirst())))
    }

    @Test
    void generateWithOperationParams()
    {
        Either<Object, OperationResult> generate = generateOperation(
                "testtableAdmin", "All records", "Filter", "", [name:"b", (SEARCH_PARAM): "true", (SEARCH_PRESETS_PARAM): "name"])

        assertEquals("{" +
                "'values':{'name':'b','value':'','_search_presets_':'name','_search_':true}," +
                "'meta':{" +
                "'/name':{'displayName':'name','readOnly':true,'canBeNull':true}," +
                "'/value':{'displayName':'value','type':'Integer','canBeNull':true}," +
                "'/_search_presets_':{'displayName':'_search_presets_','hidden':true,'readOnly':true,'canBeNull':true}," +
                "'/_search_':{'displayName':'_search_','type':'Boolean','hidden':true,'readOnly':true,'canBeNull':true}}," +
                "'order':['/name','/value','/_search_presets_','/_search_']" +
                "}", oneQuotes(JsonFactory.bean(generate.getFirst())))
    }

    @Test
    void execute()
    {
        Either<Object, OperationResult> execute = executeOperation(
                "testtableAdmin", "All records", "Filter", "", [name:"test"])

        assertEquals("table",
                oneQuotes(execute.getSecond().getStatus()))

        assertEquals("[_search_presets_:name, name:test, _search_:true]",
                oneQuotes(((TablePresentation)execute.getSecond().getDetails()).getParameters().toString()))
    }

    @Test
    void executeOldRedirectFilter()
    {
        Either<Object, OperationResult> execute = executeOperation(
                "testtableAdmin", "All records", "OldRedirectFilter", "", [name:"test"])

        assertEquals("redirect",
                oneQuotes(execute.getSecond().getStatus()))

        assertEquals("table/testtableAdmin/All records/_search_presets_=name/name=test/_search_=true",
                (String)execute.getSecond().getDetails())
    }

}