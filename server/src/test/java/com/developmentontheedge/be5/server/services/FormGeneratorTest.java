package com.developmentontheedge.be5.server.services;

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
        Map<String, Object> map = new HashMap<String, Object>(){{
            put("name", "test1");
            put("value", "2");
        }};

        ResourceData result = formGenerator.generate("testtable", "All records", "Insert", Collections.emptyMap(), map);

        assertEquals(FORM_ACTION, result.getType());

        //result.getAttributes()

        assertEquals("{'bean':{'values':{'name':'test1','value':'2'},'meta':{'/name':{'displayName':'name','columnSize':'20'},'/value':{'displayName':'value','columnSize':'30'}},'order':['/name','/value']}," + "'entity':'testtable','layout':{},'operation':'Insert','operationParams':{},'operationResult':{'status':'generate'},'query':'All records','title':'testtable: Добавить'}",
                oneQuotes(jsonb.toJson(result.getAttributes())));

        assertEquals("form/testtable/All records/Insert", result.getLinks().get(RestApiConstants.SELF_LINK));
    }

    @Test
    public void executeForm()
    {
        Map<String, Object> map = new HashMap<String, Object>(){{
            put("name", "test1");
            put("value", "2");
        }};

        ResourceData result = formGenerator.execute("testtable", "All records", "Insert", Collections.emptyMap(), map);
        assertEquals(OPERATION_RESULT, result.getType());
        assertEquals(OperationStatus.FINISHED, ((OperationResultPresentation)result.getAttributes())
                .getOperationResult().getStatus());
        verify(Be5EventTestLogger.mock).operationCompleted(any(), any(), anyLong(), anyLong());
    }

    @Test
    public void executeWithGenerateErrorInProperty()
    {
        Map<String, Object> map = new HashMap<String, Object>(){{
            put("name", "generateErrorInProperty");
        }};

        ResourceData result = formGenerator.execute("testtableAdmin", "All records", "ServerErrorProcessing", Collections.emptyMap(), map);
        FormPresentation formPresentation = (FormPresentation) result.getAttributes();
        assertEquals("{'displayName':'name','columnSize':'30','status':'error','message':'Error in property (getParameters)'}", BaseTest.oneQuotes(formPresentation.getBean().getJsonObject("meta").getJsonObject("/name").toString()));

        assertEquals(OperationStatus.ERROR, formPresentation.getOperationResult().getStatus());
        assertEquals("Error in property (getParameters)", formPresentation.getOperationResult().getMessage());

        assertNull(formPresentation.getOperationResult().getDetails());
        verify(Be5EventTestLogger.mock).operationError(any(), any(), anyLong(), anyLong(), any());
    }

    @Test
    public void testSelfLink()
    {
        Long id = db.oneLong("select id from testtable limit 1");
        assertNotNull(id);
        ResourceData result = formGenerator.generate("testtable", "All records", "Edit",
                Collections.singletonMap(OperationConstants.SELECTED_ROWS, id.toString()),
                Collections.emptyMap());

        assertEquals("form/testtable/All records/Edit/_selectedRows_=" + id.toString(),
                result.getLinks().get(RestApiConstants.SELF_LINK));
    }
}
