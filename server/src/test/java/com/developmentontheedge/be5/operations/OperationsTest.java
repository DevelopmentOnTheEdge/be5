package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.api.Request;
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


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

public class OperationsTest extends AbstractProjectTest
{
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
    public void before(){
        SqlServiceMock.clearMock();
    }

    @Test
    public void deleteOperation(){
        Request req = getSpyMockRecForOp("testtableAdmin", "All records", "Delete", "1", "");

        assertEquals(OperationResult.redirect("table/testtableAdmin/All records"),
                operationService.generate(req).getSecond());

        verify(SqlServiceMock.mock).update("DELETE FROM testtableAdmin WHERE ID IN (?)", 1L);
    }

    @Test
    public void insertOperationInitValues()
    {
        Request req = getSpyMockRecForOp("testtableAdmin", "All records", "Insert", "1","{}");

        FormPresentation first = operationService.generate(req).getFirst();
        assertEquals("{'name':'','value':''}",
                oneQuotes(first.getBean().getJsonObject("values").toString()));
    }

    @Test
    public void insertOperation(){
        Request req = getSpyMockRecForOp("testtableAdmin", "All records", "Insert", "1",
                "{'name':'test','value':1}");

        FormPresentation first = operationService.generate(req).getFirst();
        assertEquals("{" +
                        "'values':{'name':'test','value':'1'}," +
                        "'meta':{" +
                            "'/name':{'displayName':'name'}," +
                            "'/value':{'displayName':'value','type':'Integer'}}," +
                        "'order':['/name','/value']}",
                oneQuotes(first.getBean().toString()));

        //OperationResult execute = operationService.execute(req);

        assertEquals(OperationResult.redirect("table/testtableAdmin/All records"),
                operationService.execute(req).getSecond());

        verify(SqlServiceMock.mock).insert("INSERT INTO testtableAdmin (name, value) " +
                "VALUES (?, ?)", "test", 1);
    }

    @Test
    public void testGroovyOperationParameters()
    {
        Either<FormPresentation, OperationResult> generate = operationService.generate(
                getSpyMockRecForOp("testtableAdmin", "All records", "TestGroovyOp", "0","{}"));

        assertEquals("{" +
                        "'values':{'name':'','number':0}," +
                        "'meta':{" +
                        "'/name':{'displayName':'Name'}," +
                        "'/number':{" +
                        "'displayName':'Number'," +
                        "'type':'Long'," +
                        "'tagList':{'A':1,'B':2,'C':3,'D':4}," +
                        "'reloadOnChange':true}}," +
                        "'order':['/name','/number']}",
                oneQuotes(generate.getFirst().getBean().toString()));
    }

}