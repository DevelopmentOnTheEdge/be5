package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import com.developmentontheedge.be5.util.Either;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class GroovyOperationTest extends AbstractProjectTest
{
    @Inject private OperationService operationService;

    @Before
    public void beforeClass(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);
    }

    @After
    public void afterClass(){
        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    @Before
    public void before()
    {
        SqlServiceMock.clearMock();
    }

    @Test
    public void getParametersTest()
    {
        Either<FormPresentation, OperationResult> generate = operationService.generate(
                getSpyMockRecForOp("testtableAdmin", "All records", "TestGroovyOp", "0","{}"));

        assertEquals("{" +
                        "'values':{'name':'Test','beginDate':'2017-07-01','reason':'vacation','reasonMulti':['vacation','sick']}," +
                        "'meta':{" +
                            "'/name':{'displayName':'Имя'}," +
                            "'/beginDate':{'displayName':'Дата начала','type':'Date','readOnly':true}," +
                            "'/reason':{'displayName':'Причина снятия предыдущего работника'," +
                            "'tagList':[['fired','Уволен'],['vacation','Отпуск'],['sick','На больничном'],['other','Иная причина']]}," +
                            "'/reasonMulti':{'displayName':'Множественный выбор','multipleSelectionList':true,'tagList':[['fired','Уволен'],['vacation','Отпуск'],['sick','На больничном'],['other','Иная причина']]}}," +
                        "'order':['/name','/beginDate','/reason','/reasonMulti']" +
                "}", oneQuotes(generate.getFirst().getBean().toString()));
    }

    @Test
    public void getLayout()
    {
        Either<FormPresentation, OperationResult> generate = operationService.generate(
                getSpyMockRecForOp("testtableAdmin", "All records", "TestGroovyOp", "0","{}"));
        assertEquals(ImmutableMap.of("type","custom", "name","addresses"),
                generate.getFirst().getLayout());
    }

}