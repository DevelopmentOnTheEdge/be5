package com.developmentontheedge.be5.server.services

import com.developmentontheedge.be5.base.services.Meta
import com.developmentontheedge.be5.metadata.RoleType
import com.developmentontheedge.be5.operation.model.OperationStatus
import com.developmentontheedge.be5.operation.services.OperationExecutor
import com.developmentontheedge.be5.server.model.FormPresentation
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData
import org.junit.Before
import org.junit.Test

import javax.inject.Inject

import static com.developmentontheedge.be5.base.FrontendConstants.FORM_ACTION
import static org.junit.Assert.assertEquals


class FormGeneratorTest extends TestTableQueryDBTest
{
    @Inject
    private Meta meta
    @Inject
    private FormGenerator formGenerator
    @Inject
    private OperationExecutor operationExecutor

    @Before
    void setUp()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR)
    }

    @Test
    void generateForm()
    {
        ResourceData result = formGenerator.generate("testtable", "All records", "Insert",
                [] as String[], [:], [name: "test1", value: "2"])

        assertEquals(FORM_ACTION, result.getType())

        //result.getAttributes()

        assertEquals("{'bean':{'values':{'name':'test1','value':'2'},'meta':{'/name':{'displayName':'name','columnSize':'20'},'/value':{'displayName':'value','columnSize':'30'}},'order':['/name','/value']}," +
                "'entity':'testtable','layout':{},'operation':'Insert','operationParams':{},'operationResult':{'status':'generate'},'query':'All records','selectedRows':'','title':'Добавить'}",
                oneQuotes(jsonb.toJson(result.attributes)))
    }

    @Test
    void executeWithGenerateErrorInProperty()
    {
        ResourceData result = formGenerator
                .execute("testtableAdmin", "All records", "ServerErrorProcessing",
                [] as String[], [:], ['name': 'generateErrorInProperty'])
        def formPresentation = (FormPresentation) result.getAttributes()
        assertEquals "{'displayName':'name','columnSize':'30','status':'error','message':'Error in property (getParameters)'}",
                oneQuotes(formPresentation.getBean().getJsonObject("meta").getJsonObject("/name").toString())

        assertEquals OperationStatus.ERROR, formPresentation.getOperationResult().getStatus()
        assertEquals "Error in property (getParameters)",// - [ name: 'name', type: class java.lang.String, value: generateErrorInProperty (String) ]",
                formPresentation.getOperationResult().getMessage()

        assertEquals null, formPresentation.getOperationResult().getDetails()
    }
}