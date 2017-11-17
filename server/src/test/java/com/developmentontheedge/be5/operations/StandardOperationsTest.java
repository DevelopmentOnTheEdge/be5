package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.test.SqlMockOperationTest;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StandardOperationsTest extends SqlMockOperationTest
{
    @Test
    public void deleteOperation()
    {
        assertEquals(OperationResult.finished(),
                generateOperation("testtableAdmin", "All records", "Delete", "1", "").getSecond());

        verify(SqlServiceMock.mock).update("DELETE FROM testtableAdmin WHERE ID IN (?)", 1L);

        generateOperation("testtableAdmin", "All records", "Delete",
                "1,2,3", "").getSecond();
        verify(SqlServiceMock.mock).update("DELETE FROM testtableAdmin WHERE ID IN (?, ?, ?)", 1L, 2L, 3L);
    }

    @Test
    public void insertOperationInitValues()
    {
        FormPresentation first = generateOperation("testtableAdmin", "All records", "Insert", "","{}").getFirst();
        assertEquals("{'name':'','value':''}",
                oneQuotes(first.getBean().getJsonObject("values").toString()));
    }

    @Test
    public void insertOperation()
    {
        FormPresentation first = generateOperation("testtableAdmin", "All records", "Insert", "",
                "{'name':'test','value':1}").getFirst();
        assertEquals("{" +
                        "'values':{'name':'test','value':'1'}," +
                        "'meta':{" +
                            "'/name':{'displayName':'name'}," +
                            "'/value':{'displayName':'value','type':'Integer','canBeNull':true}}," +
                        "'order':['/name','/value']}",
                oneQuotes(first.getBean().toString()));

        //OperationResult execute = executeOperation(req);

        assertEquals(OperationResult.redirect("table/testtableAdmin/All records"),
                executeOperation("testtableAdmin", "All records", "Insert", "",
                        "{'name':'test','value':1}").getSecond());

        verify(SqlServiceMock.mock).insert("INSERT INTO testtableAdmin (name, value) " +
                "VALUES (?, ?)", "test", 1);
    }

    @Test
    public void editOperationGenerate()
    {
        when(SqlServiceMock.mock.select(any(),any(),any())).thenReturn(getDps(ImmutableMap.of(
                "name", "TestName",
                "value", 1,
                "ID", 12L
        )));

        FormPresentation first = generateOperation("testtableAdmin", "All records", "Edit", "12","{}").getFirst();

        verify(SqlServiceMock.mock).select(eq("SELECT * FROM testtableAdmin WHERE ID =?"),any(),eq(12L));

        assertEquals("{'name':'TestName','value':1}",
                oneQuotes(first.getBean().getJsonObject("values").toString()));
    }

    @Test
    public void editOperationGenerateStringPrimaryKey()
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport();
        dpsHelper.addDpExcludeAutoIncrement(dps, meta.getEntity("propertyTypes"));
        dps.setValue("name", "TestName");
        dps.setValue("CODE", "02");
        when(SqlServiceMock.mock.select(any(),any(),any())).thenReturn(dps);

        FormPresentation first = generateOperation("propertyTypes", "All records", "Edit", "01","{}").getFirst();

        verify(SqlServiceMock.mock).select(eq("SELECT * FROM propertyTypes WHERE CODE =?"),any(),eq("01"));

        assertEquals("{'CODE':'02','name':'TestName'}",
                oneQuotes(first.getBean().getJsonObject("values").toString()));
    }

    @Test
    public void editInvoke()
    {
        when(SqlServiceMock.mock.select(any(),any(),any())).thenReturn(getDps(ImmutableMap.of(
            "name", "TestName",
            "value", 1,
            "ID", 12L
        )));

        OperationResult operationResult = executeOperation("testtableAdmin", "All records", "Edit", "12",
                "{'name':'EditName','value':123}").getSecond();

        assertEquals(OperationResult.redirect("table/testtableAdmin/All records"),
                operationResult);

        verify(SqlServiceMock.mock).select(eq("SELECT * FROM testtableAdmin WHERE ID =?"),any(),eq(12L));

        verify(SqlServiceMock.mock).update("UPDATE testtableAdmin SET name =?, value =? WHERE ID =?",
                "EditName", 123, 12L);
    }

    @Test
    public void editInvokeValueNull()
    {
        when(SqlServiceMock.mock.select(any(),any(),any())).thenReturn(getDps(ImmutableMap.of(
                "name", "TestName",
                "value", 12345,
                "ID", 12L
        )));

        OperationResult operationResult = executeOperation("testtableAdmin", "All records", "Edit", "12",
                "{'name':'EditName','value':null}").getSecond();

        assertEquals(OperationResult.redirect("table/testtableAdmin/All records"),
                operationResult);

        verify(SqlServiceMock.mock).select(eq("SELECT * FROM testtableAdmin WHERE ID =?"),any(),eq(12L));

        verify(SqlServiceMock.mock).update("UPDATE testtableAdmin SET name =?, value =? WHERE ID =?",
                "EditName", null, 12L);
    }

    @Test
    public void editInvokeEmptyStringToNull()
    {
        when(SqlServiceMock.mock.select(any(),any(),any())).thenReturn(getDps(ImmutableMap.of(
                "name", "TestName",
                "value", 12345,
                "ID", 12L
        )));

        OperationResult operationResult = executeOperation("testtableAdmin", "All records", "Edit", "12",
                "{'name':'EditName','value':''}").getSecond();

        assertEquals(OperationResult.redirect("table/testtableAdmin/All records"),
                operationResult);

        verify(SqlServiceMock.mock).select(eq("SELECT * FROM testtableAdmin WHERE ID =?"),any(),eq(12L));

        verify(SqlServiceMock.mock).update("UPDATE testtableAdmin SET name =?, value =? WHERE ID =?",
                "EditName", null, 12L);
    }


}