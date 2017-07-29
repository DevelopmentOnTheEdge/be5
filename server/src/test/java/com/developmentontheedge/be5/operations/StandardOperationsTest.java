package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.api.helpers.SqlHelper;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import com.developmentontheedge.beans.DynamicPropertySet;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StandardOperationsTest extends AbstractProjectTest
{
    private OperationService operationService = sqlMockInjector.get(OperationService.class);
    private SqlHelper sqlHelper = sqlMockInjector.get(SqlHelper.class);
    private Meta meta = sqlMockInjector.get(Meta.class);

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

        verify(SqlServiceMock.mock).update("DELETE FROM testtableAdmin WHERE ID IN (?)", "1");

        operationService.generate(getSpyMockRecForOp("testtableAdmin", "All records", "Delete",
                "1,2,3", "")).getSecond();
        verify(SqlServiceMock.mock).update("DELETE FROM testtableAdmin WHERE ID IN (?, ?, ?)", "1", "2", "3");
    }

    @Test
    public void insertOperationInitValues()
    {
        Request req = getSpyMockRecForOp("testtableAdmin", "All records", "Insert", "","{}");

        FormPresentation first = operationService.generate(req).getFirst();
        assertEquals("{'name':'','value':''}",
                oneQuotes(first.getBean().getJsonObject("values").toString()));
    }

    @Test
    public void insertOperation(){
        Request req = getSpyMockRecForOp("testtableAdmin", "All records", "Insert", "",
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
    public void editOperationGenerate()
    {
        Request req = getSpyMockRecForOp("testtableAdmin", "All records", "Edit", "1","{}");

        DynamicPropertySet dps = sqlHelper.getDpsWithoutAutoIncrement(meta.getEntity("testtableAdmin"));
        dps.setValue("name", "TestName");
        dps.setValue("value", 1);
        when(SqlServiceMock.mock.select(any(),any(),any())).thenReturn(dps);

        FormPresentation first = operationService.generate(req).getFirst();

        verify(SqlServiceMock.mock).select(eq("SELECT * FROM testtableAdmin WHERE ID =?"),any(),eq("1"));

        assertEquals("{'name':'TestName','value':1}",
                oneQuotes(first.getBean().getJsonObject("values").toString()));
    }

    @Test
    public void editOperationGenerateStringPrimaryKey()
    {
        Request req = getSpyMockRecForOp("propertyTypes", "All records", "Edit", "01","{}");

        DynamicPropertySet dps = sqlHelper.getDpsWithoutAutoIncrement(meta.getEntity("propertyTypes"));
        dps.setValue("name", "TestName");
        dps.setValue("CODE", "02");
        when(SqlServiceMock.mock.select(any(),any(),any())).thenReturn(dps);

        FormPresentation first = operationService.generate(req).getFirst();

        verify(SqlServiceMock.mock).select(eq("SELECT * FROM propertyTypes WHERE CODE =?"),any(),eq("01"));

        assertEquals("{'CODE':'02','name':'TestName'}",
                oneQuotes(first.getBean().getJsonObject("values").toString()));
    }

    @Test
    public void editInvoke()
    {
        Request req = getSpyMockRecForOp("testtableAdmin", "All records", "Edit", "1",
                "{'name':'EditName','value':123}");

        DynamicPropertySet dps = sqlHelper.getDpsWithoutAutoIncrement(meta.getEntity("testtableAdmin"));
        dps.setValue("name", "TestName");
        dps.setValue("value", 1);
        when(SqlServiceMock.mock.select(any(),any(),any())).thenReturn(dps);

        OperationResult operationResult = operationService.execute(req).getSecond();

        assertEquals(OperationResult.redirect("table/testtableAdmin/All records"),
                operationResult);

        verify(SqlServiceMock.mock).select(eq("SELECT * FROM testtableAdmin WHERE ID =?"),any(),eq("1"));

        verify(SqlServiceMock.mock).update("UPDATE testtableAdmin SET name =?, value =? WHERE ID =?",
                "EditName", 123, "1");
    }

}