package com.developmentontheedge.be5.server.services;

import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.operation.model.OperationStatus;
import com.developmentontheedge.be5.operation.services.OperationExecutor;
import com.developmentontheedge.be5.server.model.FormPresentation;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.test.BaseTestUtils;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.developmentontheedge.be5.base.FrontendConstants.FORM_ACTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FormGeneratorTest extends TestTableQueryDBTest
{
    @Inject private Meta meta;
    @Inject private FormGenerator formGenerator;
    @Inject private OperationExecutor operationExecutor;

    @Before
    public void setUp()
    {
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

        assertEquals("{'bean':{'values':{'name':'test1','value':'2'},'meta':{'/name':{'displayName':'name','columnSize':'20'},'/value':{'displayName':'value','columnSize':'30'}},'order':['/name','/value']}," + "'entity':'testtable','layout':{},'operation':'Insert','operationParams':{},'operationResult':{'status':'generate'},'query':'All records','title':'Добавить'}",
                oneQuotes(jsonb.toJson(result.getAttributes())));
    }

    @Test
    public void executeWithGenerateErrorInProperty()
    {
        Map<String, Object> map = new HashMap<String, Object>(){{
            put("name", "generateErrorInProperty");
        }};

        ResourceData result = formGenerator.execute("testtableAdmin", "All records", "ServerErrorProcessing", Collections.emptyMap(), map);
        FormPresentation formPresentation = (FormPresentation) result.getAttributes();
        assertEquals("{'displayName':'name','columnSize':'30','status':'error','message':'Error in property (getParameters)'}", BaseTestUtils.oneQuotes(formPresentation.getBean().getJsonObject("meta").getJsonObject("/name").toString()));

        assertEquals(OperationStatus.ERROR, formPresentation.getOperationResult().getStatus());
        assertEquals("Error in property (getParameters)", formPresentation.getOperationResult().getMessage());

        assertNull(formPresentation.getOperationResult().getDetails());
    }
}
