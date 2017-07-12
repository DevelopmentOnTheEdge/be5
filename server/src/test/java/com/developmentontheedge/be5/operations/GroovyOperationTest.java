package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import com.developmentontheedge.be5.util.Either;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class GroovyOperationTest extends AbstractProjectTest{

    private OperationService operationService = sqlMockInjector.get(OperationService.class);

    @BeforeClass
    public static void beforeClass(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);
    }

    @AfterClass
    public static void afterClass(){
        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    @Before
    public void before()
    {
        SqlServiceMock.clearMock();
    }

    @Test
    public void test()
    {
        Either<FormPresentation, OperationResult> generate = operationService.generate(
                getSpyMockRecForOp("testtableAdmin", "All records", "TestGroovyOp", "0","{}"));

        assertEquals("{" +
                        "'values':{'name':'','number':'1'}," +
                        "'meta':{" +
                        "'/name':{'displayName':'Name'}," +
                        "'/number':{" +
                        "'displayName':'Number'," +
                        "'type':'Long'," +
                        "'tagList':[['A','a'],['B','b'],['C','c'],['D','d']]," +
                        "'reloadOnChange':true}}," +
                        "'order':['/name','/number']}",
                oneQuotes(generate.getFirst().getBean().toString()));
    }

    @Test
    public void testParameters()
    {
        Either<FormPresentation, OperationResult> generate = operationService.generate(
                getSpyMockRecForOp("testtableAdmin", "All records", "TestGroovyOp", "0",
                        doubleQuotes("{'name':'testName','number':'2'}")));

        assertEquals("{" +
                        "'values':{'name':'testName','number':'2'}," +
                        "'meta':{" +
                        "'/name':{'displayName':'Name'}," +
                        "'/number':{" +
                        "'displayName':'Number'," +
                        "'type':'Long'," +
                        "'tagList':[['A','a'],['B','b'],['C','c'],['D','d']]," +
                        "'reloadOnChange':true}}," +
                        "'order':['/name','/number']}",
                oneQuotes(generate.getFirst().getBean().toString()));
    }
}