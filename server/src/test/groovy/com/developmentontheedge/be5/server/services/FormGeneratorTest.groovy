package com.developmentontheedge.be5.server.services

import com.developmentontheedge.be5.base.services.Meta
import com.developmentontheedge.be5.operation.model.OperationInfo
import com.developmentontheedge.be5.operation.model.OperationResult
import com.developmentontheedge.be5.operation.model.OperationStatus
import com.developmentontheedge.be5.operation.services.OperationExecutor
import com.developmentontheedge.be5.operation.util.Either
import com.developmentontheedge.be5.server.model.FormPresentation
import groovy.transform.TypeChecked
import org.junit.Before
import org.junit.Test

import javax.inject.Inject

import static org.junit.Assert.assertEquals


@TypeChecked
class FormGeneratorTest extends TestTableQueryDBTest
{
    @Inject private Meta meta
    @Inject private FormGenerator formGenerator
    @Inject private OperationExecutor operationExecutor

    @Before
    void setUp()
    {
        initGuest()
    }

    @Test
    void generateForm()
    {
        def result = formGenerator.generate(
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

        Either<FormPresentation, OperationResult> either = formGenerator
                .execute(operation, ['name': 'generateErrorInProperty'])

        assertEquals "{'displayName':'name','columnSize':'30','status':'error','message':'Error in property (getParameters)'}",
                oneQuotes(either.getFirst().getBean().getJsonObject("meta").getJsonObject("/name").toString())

        assertEquals OperationStatus.ERROR, operation.getResult().getStatus()
        assertEquals "Error in property (getParameters)",// - [ name: 'name', type: class java.lang.String, value: generateErrorInProperty (String) ]",
                operation.getResult().getMessage()
    }
}