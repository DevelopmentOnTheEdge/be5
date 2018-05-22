package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.test.SqlMockOperationTest;
import com.developmentontheedge.be5.test.mocks.DbServiceMock;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.developmentontheedge.beans.json.JsonFactory;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;


import java.util.Collections;

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
        assertEquals(OperationStatus.FINISHED,
                generateOperation("testtableAdmin", "All records", "Delete", "1", "").getSecond().getStatus());

        verify(DbServiceMock.mock).update("DELETE FROM testtableAdmin WHERE ID IN (?)", 1L);
        verify(DbServiceMock.mock).update("DELETE FROM testCollection WHERE categoryID IN (?)", 1L);
        verify(DbServiceMock.mock).update("DELETE FROM testGenCollection WHERE recordID IN (?)", "testtableAdmin.1");

        generateOperation("testtableAdmin", "All records", "Delete",
                "1,2,3", "").getSecond();

        verify(DbServiceMock.mock).update("DELETE FROM testtableAdmin WHERE ID IN (?, ?, ?)", 1L, 2L, 3L);
    }

    @Test
    public void insertOperationInitValues()
    {
        Operation operation = createOperation("testtableAdmin", "All records", "Insert", "");
        assertEquals(OperationStatus.CREATE, operation.getStatus());

        Object first = generateOperation(operation, "{}").getFirst();

        assertEquals(OperationStatus.GENERATE, operation.getStatus());

        assertEquals("{'name':'','value':''}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString()));
    }

    @Test
    public void insertOperation()
    {
        Object first = generateOperation("testtable", "All records", "Insert", "",
                "{'name':'test','value':1}").getFirst();
        assertEquals("{" +
                        "'values':{'name':'test','value':'1'}," +
                        "'meta':{" +
                            "'/name':{'displayName':'name','columnSize':'20'}," +
                            "'/value':{'displayName':'value','columnSize':'30'}}," +
                        "'order':['/name','/value']}",
                oneQuotes(JsonFactory.bean(first)));

        //OperationResult execute = executeOperation(req);

        assertEquals(OperationResult.redirect("table/testtable/All records"),
                executeOperation("testtable", "All records", "Insert", "",
                        "{'name':'test','value':'1'}").getSecond());

        verify(DbServiceMock.mock).insert("INSERT INTO testtable (name, value) " +
                "VALUES (?, ?)", "test", "1");
    }

    @Test
    public void editOperationGenerate()
    {
        when(DbServiceMock.mock.select(any(),any(),any())).thenReturn(getDpsS(ImmutableMap.of(
                "name", "TestName",
                "value", 1,
                "ID", 12L
        )));

        Object first = generateOperation("testtableAdmin", "All records", "Edit", "12","{}").getFirst();

        verify(DbServiceMock.mock).select(eq("SELECT * FROM testtableAdmin WHERE ID = ?"),any(),eq(12L));

        assertEquals("{'name':'TestName','value':'1'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString()));
    }

    @Test
    public void editOperationGenerateStringPrimaryKey()
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport();
        dpsHelper.addDpExcludeAutoIncrement(dps, meta.getEntity("propertyTypes"), Collections.emptyMap());
        dps.setValue("name", "TestName");
        dps.setValue("CODE", "02");
        when(DbServiceMock.mock.select(any(),any(),any())).thenReturn(dps);

        Object first = generateOperation("propertyTypes", "All records", "Edit", "01","{}").getFirst();

        verify(DbServiceMock.mock).select(eq("SELECT * FROM propertyTypes WHERE CODE = ?"),any(),eq("01"));

        assertEquals("{'CODE':'02','name':'TestName'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString()));
    }

    @Test
    public void editInvoke()
    {
        executeEditWithParams("{'name':'EditName','value':123}");

        verify(DbServiceMock.mock).update("UPDATE testtableAdmin SET name = ?, value = ? WHERE ID = ?",
                "EditName", 123, 12L);
    }

    @Test
    public void editInvokeValueNull()
    {
        executeEditWithParams("{'name':'EditName','value':null}");

        verify(DbServiceMock.mock).update("UPDATE testtableAdmin SET name = ?, value = ? WHERE ID = ?",
                "EditName", null, 12L);
    }

    @Test
    public void editInvokeEmptyStringToNull()
    {
        executeEditWithParams("{'name':'EditName','value':''}");

        verify(DbServiceMock.mock).update("UPDATE testtableAdmin SET name = ?, value = ? WHERE ID = ?",
                "EditName", null, 12L);
    }

    private void executeEditWithParams(String params)
    {
        when(DbServiceMock.mock.select(any(),any(),any())).thenReturn(getDpsS(ImmutableMap.of(
                "name", "TestName",
                "value", 12345,
                "ID", 12L
        )));

        OperationResult operationResult = executeOperation("testtableAdmin", "All records", "Edit", "12",
                params).getSecond();

        assertEquals(OperationResult.redirect("table/testtableAdmin/All records"),
                operationResult);

        verify(DbServiceMock.mock).select(eq("SELECT * FROM testtableAdmin WHERE ID = ?"),any(),eq(12L));
    }
}