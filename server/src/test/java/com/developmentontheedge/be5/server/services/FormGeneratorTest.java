package com.developmentontheedge.be5.server.services;

import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.operation.OperationConstants;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.server.RestApiConstants;
import com.developmentontheedge.be5.server.model.FormPresentation;
import com.developmentontheedge.be5.server.model.OperationResultPresentation;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.server.services.events.Be5EventTestLogger;
import com.developmentontheedge.be5.test.BaseTest;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.developmentontheedge.be5.FrontendConstants.FORM_ACTION;
import static com.developmentontheedge.be5.FrontendConstants.OPERATION_RESULT;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;

public class FormGeneratorTest extends TestTableQueryDBTest
{
    @Inject private FormGenerator formGenerator;

    @Before
    public void setUp()
    {
        Be5EventTestLogger.clearMock();
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR);
    }

    @Test
    public void generateForm()
    {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "test1");
        map.put("value", "2");

        ResourceData result = formGenerator.generate("testtable", "All records", "Insert", emptyMap(), map);

        assertEquals(FORM_ACTION, result.getType());

        //result.getAttributes()

        assertEquals("{'bean':{'values':{'name':'test1','value':'2'},'meta':{'/name':{'displayName':'Name','columnSize':'20'},'/value':{'displayName':'Value','columnSize':'30'}},'order':['/name','/value']}," + "'entity':'testtable','layout':{},'operation':'Insert','operationParams':{},'operationResult':{'status':'GENERATE','timeout':5},'query':'All records','title':'Testtable: Добавить'}",
                oneQuotes(jsonb.toJson(result.getAttributes())));

        assertEquals("form/testtable/All records/Insert", result.getLinks().get(RestApiConstants.SELF_LINK));
    }

    @Test
    public void getParametersEdit()
    {
        db.update("delete from testtableAdmin");
        db.insert("insert into testtableAdmin (name, value) VALUES (?, ?)", "tableModelTest", 12);

        Long id = db.one("select id from testtableAdmin limit 1");
        assertNotNull(id);

        ResourceData result = formGenerator.generate("testtableAdmin", "All records", "Edit",
                Collections.singletonMap(OperationConstants.SELECTED_ROWS, id),
                emptyMap());

        assertEquals(FORM_ACTION, result.getType());

        assertEquals("{'bean':{'values':{'name':'tableModelTest','value':'12'},'meta':{'/name':{'displayName':'Name','columnSize':'30'},'/value':{'displayName':'Value','type':'Integer','canBeNull':true,'validationRules':[{'attr':{'max':'2147483647','min':'-2147483648'},'type':'range'},{'attr':'1','type':'step'}]}},'order':['/name','/value']},'entity':'testtableAdmin','layout':{},'operation':'Edit','operationParams':{'_selectedRows_':" + id + "},'operationResult':{'status':'GENERATE','timeout':5},'query':'All records','title':'testtableAdmin: Редактировать'}",
                oneQuotes(jsonb.toJson(result.getAttributes())));
        Map<String, Object> oparams = ((FormPresentation)result.getAttributes()).getOperationParams();
        assertEquals(1, oparams.size());
        assertEquals("_selectedRows_", oparams.keySet().iterator().next());
        assertEquals("form/testtableAdmin/All records/Edit/_selectedRows_=" + oparams.values().iterator().next(), result.getLinks().get(RestApiConstants.SELF_LINK));
    }

    @Test
    public void getParametersInsert()
    {
        ResourceData result = formGenerator.generate("testtableAdmin", "All records", "Insert",
                emptyMap(),
                emptyMap());

        assertEquals(FORM_ACTION, result.getType());

        assertEquals("{'bean':{'values':{'name':'','value':'111'},'meta':{'/name':{'displayName':'Name','columnSize':'30'},'/value':{'displayName':'Value','type':'Integer','canBeNull':true,'validationRules':[{'attr':{'max':'2147483647','min':'-2147483648'},'type':'range'},{'attr':'1','type':'step'}]}},'order':['/name','/value']},'entity':'testtableAdmin','layout':{'type':'modalForm'},'operation':'Insert','operationParams':{},'operationResult':{'status':'GENERATE','timeout':5},'query':'All records','title':'testtableAdmin: Добавить'}",
                oneQuotes(jsonb.toJson(result.getAttributes())));
        assertEquals("form/testtableAdmin/All records/Insert", result.getLinks().get(RestApiConstants.SELF_LINK));
    }

    @Test
    public void getParametersEdit2Records()
    {
        db.update("delete from testtableAdmin");
        db.insert("insert into testtableAdmin (name, value) VALUES (?, ?)", "tableModelTest", 12);
        db.insert("insert into testtableAdmin (name, value) VALUES (?, ?)", "tableModelTest", 13);

        Long[] ids = db.longArray("select id from testtableAdmin limit 2");
        assertEquals(2, ids.length);
        Long id1 = ids[0];
        Long id2 = ids[1];
        assertNotNull(id1);
        assertNotNull(id2);

        ResourceData result = formGenerator.generate("testtableAdmin", "All records", "Edit",
                Collections.singletonMap(OperationConstants.SELECTED_ROWS, ids),
                emptyMap());

        assertEquals(FORM_ACTION, result.getType());

        assertEquals("{'bean':{'values':{'name':'','value':''},'meta':{'/name':{'displayName':'Name','canBeNull':true,'columnSize':'30'},'/value':{'displayName':'Value','type':'Integer','canBeNull':true,'validationRules':[{'attr':{'max':'2147483647','min':'-2147483648'},'type':'range'},{'attr':'1','type':'step'}]}},'order':['/name','/value']},'entity':'testtableAdmin','layout':{},'operation':'Edit','operationParams':{'_selectedRows_':[" + id1 + "," + id2 + "]},'operationResult':{'status':'GENERATE','timeout':5},'query':'All records','title':'testtableAdmin: Редактировать'}",
                oneQuotes(jsonb.toJson(result.getAttributes())));
        Map<String, Object> oparams = ((FormPresentation)result.getAttributes()).getOperationParams();
        assertEquals(1, oparams.size());
        assertEquals("_selectedRows_", oparams.keySet().iterator().next());
        assertEquals("form/testtableAdmin/All records/Edit/_selectedRows_=" + oparams.values().iterator().next(), result.getLinks().get(RestApiConstants.SELF_LINK));
    }

    @Test(expected = Be5Exception.class)
    public void fileNotFoundError()
    {
        formGenerator.generate("testtable", "All records", "FileNotFoundError", emptyMap(), emptyMap());
    }

    @Test
    public void executeForm()
    {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "test1");
        map.put("value", "2");

        ResourceData result = formGenerator.execute("testtable", "All records", "Insert", emptyMap(), map);
        assertEquals(OPERATION_RESULT, result.getType());
        assertEquals(OperationStatus.FINISHED, ((OperationResultPresentation)result.getAttributes())
                .getOperationResult().getStatus());
        verify(Be5EventTestLogger.mock).operationCompleted(any(), any(), anyLong(), anyLong());
    }

    @Test
    public void executeWithGenerateErrorInProperty()
    {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "generateErrorInProperty");

        ResourceData result = formGenerator.execute("testtableAdmin", "All records", "ServerErrorProcessing", emptyMap(), map);
        FormPresentation formPresentation = (FormPresentation) result.getAttributes();
        assertEquals("{'displayName':'Name','columnSize':'30','status':'error','message':'Error in property (getParameters)'}", BaseTest.oneQuotes(formPresentation.getBean().getJsonObject("meta").getJsonObject("/name").toString()));

        assertEquals(OperationStatus.ERROR, formPresentation.getOperationResult().getStatus());
        assertEquals("Error in property (getParameters)", formPresentation.getOperationResult().getMessage());
        assertEquals(5, formPresentation.getOperationResult().getTimeout() );

        assertNull(formPresentation.getOperationResult().getDetails());
        verify(Be5EventTestLogger.mock).operationError(any(), any(), anyLong(), anyLong(), any());
    }

    @Test
    public void executeWithGenerateErrorWithTimeoutInProperty()
    {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "generateErrorWithTimeout");

        ResourceData result = formGenerator.execute("testtableAdmin", "All records", "ServerErrorProcessing", emptyMap(), map);
        OperationResultPresentation operationResultPresentation = (OperationResultPresentation) result.getAttributes();
        assertEquals("OperationResult{status=error, message='Error in property (getParameters) with timeout 20', details=null, timeout=20}", BaseTest.oneQuotes(operationResultPresentation.getOperationResult().toString()));

        assertEquals(OperationStatus.ERROR, operationResultPresentation.getOperationResult().getStatus());
        assertEquals("Error in property (getParameters) with timeout 20", operationResultPresentation.getOperationResult().getMessage());
        assertEquals(20, operationResultPresentation.getOperationResult().getTimeout() );

        assertNull(operationResultPresentation.getOperationResult().getDetails());
        verify(Be5EventTestLogger.mock).operationError(any(), any(), anyLong(), anyLong(), any());
    }

    @Test
    public void selfLink()
    {
        Long id = db.oneLong("select id from testtable limit 1");
        assertNotNull(id);
        ResourceData result = formGenerator.generate("testtable", "All records", "Edit",
                Collections.singletonMap(OperationConstants.SELECTED_ROWS, new String[]{id.toString()}),
                emptyMap());

        assertEquals("form/testtable/All records/Edit/_selectedRows_=" + id.toString(),
                result.getLinks().get(RestApiConstants.SELF_LINK));
    }

    @Test
    public void filterNoTitle()
    {
        Long id = db.oneLong("select id from testtable limit 1");
        assertNotNull(id);
        ResourceData result = formGenerator.generate("testtable", "All records", "FilterNoTitle", emptyMap(), emptyMap());

        assertEquals("{'bean':{'values':{'name':'','value':'','_search_presets_':'','_search_':true},'meta':{'/name':{'displayName':'Name','canBeNull':true,'columnSize':'20'},'/value':{'displayName':'Value','canBeNull':true,'columnSize':'30'},'/_search_presets_':{'displayName':'_search_presets_','hidden':true,'readOnly':true,'canBeNull':true},'/_search_':{'displayName':'_search_','type':'Boolean','hidden':true,'readOnly':true,'canBeNull':true}},'order':['/name','/value','/_search_presets_','/_search_']},'entity':'testtable','layout':{'type':'modalForm','title':'none'},'operation':'FilterNoTitle','operationParams':{},'operationResult':{'status':'GENERATE','timeout':5},'query':'All records','title':''}",
                oneQuotes(jsonb.toJson(result.getAttributes())));
    }

    @Test
    public void topFormTitle()
    {
        Long id = db.oneLong("select id from testtable limit 1");
        assertNotNull(id);
        ResourceData result = formGenerator.generate("testtable", "TableWithFilter", "FilterByParamsInQueryOperation", emptyMap(), emptyMap());

        assertEquals("{'bean':{'values':{'_search_presets_':'','_search_':true},'meta':{'/_search_presets_':{'displayName':'_search_presets_','hidden':true,'readOnly':true,'canBeNull':true},'/_search_':{'displayName':'_search_','type':'Boolean','hidden':true,'readOnly':true,'canBeNull':true}},'order':['/_search_presets_','/_search_']},'entity':'testtable','layout':{'type':'modalForm'},'operation':'FilterByParamsInQueryOperation','operationParams':{},'operationResult':{'status':'GENERATE','timeout':5},'query':'TableWithFilter','title':'FilterByParamsInQueryOperation'}",
                oneQuotes(jsonb.toJson(result.getAttributes())));
    }

    @Test
    public void filterTitleFromLayout()
    {
        Long id = db.oneLong("select id from testtable limit 1");
        assertNotNull(id);
        ResourceData result = formGenerator.generate("testtable", "All records", "FilterTitleFromLayout", emptyMap(), emptyMap());

        assertEquals("{'bean':{'values':{'name':'','value':'','_search_presets_':'','_search_':true},'meta':{'/name':{'displayName':'Name','canBeNull':true,'columnSize':'20'},'/value':{'displayName':'Value','canBeNull':true,'columnSize':'30'},'/_search_presets_':{'displayName':'_search_presets_','hidden':true,'readOnly':true,'canBeNull':true},'/_search_':{'displayName':'_search_','type':'Boolean','hidden':true,'readOnly':true,'canBeNull':true}},'order':['/name','/value','/_search_presets_','/_search_']},'entity':'testtable','layout':{'type':'modalForm','title':'Title from layout'},'operation':'FilterTitleFromLayout','operationParams':{},'operationResult':{'status':'GENERATE','timeout':5},'query':'All records','title':'Имя из layout'}",
                oneQuotes(jsonb.toJson(result.getAttributes())));
    }
}
