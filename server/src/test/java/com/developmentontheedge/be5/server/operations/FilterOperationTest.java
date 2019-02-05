package com.developmentontheedge.be5.server.operations;

import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.util.Either;
import com.developmentontheedge.be5.server.model.FrontendAction;
import com.developmentontheedge.be5.server.model.TablePresentation;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.test.SqlMockOperationTest;
import com.developmentontheedge.beans.json.JsonFactory;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.developmentontheedge.be5.FrontendConstants.SEARCH_PARAM;
import static com.developmentontheedge.be5.FrontendConstants.SEARCH_PRESETS_PARAM;
import static com.developmentontheedge.be5.server.FrontendActions.CLOSE_MAIN_MODAL;
import static com.developmentontheedge.be5.server.FrontendActions.UPDATE_PARENT_DOCUMENT;
import static org.junit.Assert.assertEquals;

public class FilterOperationTest extends SqlMockOperationTest
{
    @Test
    public void generate()
    {
        Operation operation = createOperation("testtable", "All records", "Filter", "");
        Either<Object, OperationResult> generate = generateOperation(operation, Collections.emptyMap());

        assertEquals("{" +
                "'values':{'name':'','value':'','_search_presets_':'','_search_':true}," +
                "'meta':{" +
                "'/name':{'displayName':'name','canBeNull':true,'columnSize':'20'}," +
                "'/value':{'displayName':'value','canBeNull':true,'columnSize':'30'}," +
                "'/_search_presets_':{'displayName':'_search_presets_','hidden':true,'readOnly':true,'canBeNull':true}," +
                "'/_search_':{'displayName':'_search_','type':'Boolean','hidden':true,'readOnly':true,'canBeNull':true}}," +
                "'order':['/name','/value','/_search_presets_','/_search_']" +
                "}", oneQuotes(JsonFactory.bean(generate.getFirst())));
    }

    @Test
    public void generateWithOperationParams()
    {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>(3);
        map.put("name", "b");
        map.put(SEARCH_PARAM, "true");
        map.put(SEARCH_PRESETS_PARAM, "name");
        Operation operation = createOperation("testtable", "All records", "Filter", map);
        Either<Object, OperationResult> generate = generateOperation(operation, "");

        assertEquals("{" +
                "'values':{'name':'b','value':'','_search_presets_':'name','_search_':true}," +
                "'meta':{" +
                "'/name':{'displayName':'name','readOnly':true,'canBeNull':true,'columnSize':'20'}," +
                "'/value':{'displayName':'value','canBeNull':true,'columnSize':'30'}," +
                "'/_search_presets_':{'displayName':'_search_presets_','hidden':true,'readOnly':true,'canBeNull':true}," +
                "'/_search_':{'displayName':'_search_','type':'Boolean','hidden':true,'readOnly':true,'canBeNull':true}}," +
                "'order':['/name','/value','/_search_presets_','/_search_']" +
                "}", oneQuotes(JsonFactory.bean(generate.getFirst())));
    }

    @Test
    public void generateDocumentForm()
    {
        Map<String, Object> map = new LinkedHashMap<>(3);
        map.put("_search_", true);
        map.put("_search_presets_", "");
        map.put("name", "name1");
        Operation operation = createOperation("testtable", "All records", "Filter", map);
        Either<Object, OperationResult> generate = generateOperation(operation, Collections.emptyMap());

        assertEquals("{'name':'name1','value':'','_search_presets_':'','_search_':true}",
                oneQuotes(JsonFactory.beanValues(generate.getFirst())));
    }

    @Test
    public void executeFilterWithoutOperationParamProperty()
    {
        Operation operation = createOperation("testtable", "TestFilterByParamsInQueryOperation", "FilterByParamsInQueryOperation",
                Collections.singletonMap("value", "value1"));
        Either<Object, OperationResult> execute = executeOperation(operation, Collections.emptyMap());

        assertEquals("finished", oneQuotes(execute.getSecond().getStatus()));
        FrontendAction[] details = (FrontendAction[]) execute.getSecond().getDetails();
        assertEquals("{_search_presets_=value, _search_=true, value=value1}",
                oneQuotes(((TablePresentation) ((JsonApiModel) details[0].getValue()).getData().getAttributes()).getParameters().toString()));
    }

    @Test
    public void executeFilterWithoutOperationParamProperty2()
    {
        Operation operation = createOperation("testtable", "TestFilterByParamsInQueryOperation", "FilterByParamsInQueryOperation",
                Collections.singletonMap("value", "value1"));
        LinkedHashMap<String, Object> map1 = new LinkedHashMap<>(3);
        map1.put("_search_", true);
        map1.put("_search_presets_", "value");
        map1.put("name", "name1");
        Either<Object, OperationResult> execute = executeOperation(operation, map1);

        assertEquals("finished", oneQuotes(execute.getSecond().getStatus()));
        FrontendAction[] details = (FrontendAction[]) execute.getSecond().getDetails();
        assertEquals("{name=name1, _search_presets_=value, _search_=true, value=value1}",
                oneQuotes(((TablePresentation) ((JsonApiModel) details[0].getValue()).getData().getAttributes()).getParameters().toString()));
    }

    @Test
    public void executeFilterWithoutOperationParamProperty3()
    {
        Operation operation = createOperation("testtable", "TestFilterByParamsInQueryOperation", "FilterByParamsInQueryOperation",
                Collections.singletonMap("value", "value1"));
        Either<Object, OperationResult> execute = executeOperation(operation, Collections.singletonMap("name", "name1"));

        assertEquals("finished", oneQuotes(execute.getSecond().getStatus()));
        FrontendAction[] details = (FrontendAction[]) execute.getSecond().getDetails();
        assertEquals("{name=name1, _search_presets_=value, _search_=true, value=value1}",
                oneQuotes(((TablePresentation) ((JsonApiModel) details[0].getValue()).getData().getAttributes()).getParameters().toString()));
    }

    @Test
    public void changeFilterTest()
    {
        Map<String, Object> map = new LinkedHashMap<>(3);
        map.put("_search_", true);
        map.put("_search_presets_", "");
        map.put("name", "name1");
        Operation operation = createOperation("testtable", "TestFilterByParamsInQueryOperation", "FilterByParamsInQueryOperation", map);
        Map<String, Object> map1 = new LinkedHashMap<>(3);
        map1.put("_search_", true);
        map1.put("_search_presets_", "");
        map1.put("name", "name2");
        Either<Object, OperationResult> execute = executeOperation(operation, map1);

        assertEquals("finished", oneQuotes(execute.getSecond().getStatus()));
        FrontendAction[] details = (FrontendAction[]) execute.getSecond().getDetails();
        assertEquals("{name=name2, _search_presets_=, _search_=true}", oneQuotes(((TablePresentation) ((JsonApiModel) details[0].getValue()).getData().getAttributes()).getParameters().toString()));
    }

    @Test
    public void execute()
    {
        Operation operation = createOperation("testtable", "All records", "Filter", Collections.singletonMap("name", "test"));
        Either<Object, OperationResult> execute = executeOperation(operation, Collections.emptyMap());

        assertEquals("finished", oneQuotes(execute.getSecond().getStatus()));

        FrontendAction[] details = (FrontendAction[]) execute.getSecond().getDetails();

        assertEquals("{name=test, _search_presets_=name, _search_=true}",
                oneQuotes(((TablePresentation) ((JsonApiModel) details[0].getValue()).getData().getAttributes()).getParameters().toString()));

        assertEquals(UPDATE_PARENT_DOCUMENT, details[0].getType());
        assertEquals(CLOSE_MAIN_MODAL, details[1].getType());
    }

    @Test
    public void executeOldRedirectFilter()
    {
        Operation operation = createOperation("testtable", "All records", "OldRedirectFilter", Collections.singletonMap("name", "test"));
        Either<Object, OperationResult> execute = executeOperation(operation, Collections.emptyMap());

        assertEquals("redirect", oneQuotes(execute.getSecond().getStatus()));

        assertEquals("table/testtable/All records/name=test", execute.getSecond().getDetails());
    }

}
