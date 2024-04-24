package com.developmentontheedge.be5.server.operations;

import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.database.QRec;
import com.developmentontheedge.be5.server.model.FrontendAction;
import com.developmentontheedge.be5.test.SqlMockOperationTest;
import com.developmentontheedge.be5.test.mocks.DbServiceMock;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.developmentontheedge.beans.json.JsonFactory;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.developmentontheedge.be5.server.FrontendActions.GO_BACK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


public class StandardOperationsTest extends SqlMockOperationTest
{
    @Test
    public void simpleTestOperation()
    {
        OperationResult result = executeOperation("testtableAdmin", "All records",
                "SimpleTestOperation", "", "").getSecond();
        assertEquals(OperationStatus.FINISHED, result.getStatus());
        assertEquals(GO_BACK, ((FrontendAction[])result.getDetails())[0].getType());
        assertEquals("table/testtableAdmin/All records", ((FrontendAction[])result.getDetails())[0].getValue());
    }

    @Test
    public void deleteOperation()
    {
        whenSelectListTagsContains("FROM testtableAdmin WHERE testtableAdmin.ID = 1 LIMIT 2147483647", "1");

        assertEquals(OperationStatus.FINISHED,
                executeOperation("testtableAdmin", "All records", "Delete", "1", "").getSecond().getStatus());

        verify(DbServiceMock.mock).updateRaw("DELETE FROM testtableAdmin WHERE ID IN (?)", 1L);
        verify(DbServiceMock.mock).updateRaw("UPDATE testGenCollection SET isDeleted___ = ? WHERE recordID IN (?)", "yes", "testtableAdmin.1");
    }

    @Test
    public void deleteOperationRestoredRecords()
    {
        whenSelectListTagsContains("FROM testRestoredRecords WHERE testRestoredRecords.ID = 1 LIMIT 2147483647", "1");

        assertEquals(OperationStatus.FINISHED,
                executeOperation("testRestoredRecords", "All records", "Delete", "1", "").getSecond().getStatus());

        verify(DbServiceMock.mock).updateRaw("UPDATE testRestoredRecords SET isDeleted___ = ? WHERE ID IN (?)", "yes", 1L);
        verify(DbServiceMock.mock).updateRaw("UPDATE testGenCollection SET isDeleted___ = ? WHERE recordID IN (?)", "yes", "testRestoredRecords.1");
        verify(DbServiceMock.mock).updateRaw("UPDATE testCollection SET isDeleted___ = ? WHERE categoryID IN (?)", "yes", 1L);
    }

    @Test
    public void restoreOperationRestoredRecords()
    {
        whenSelectListTagsContains("FROM testRestoredRecords WHERE testtableAdmin.ID = 1 LIMIT 2147483647", "1");

        assertEquals(OperationStatus.FINISHED,
                executeOperation("testRestoredRecords", "All records", "Restore", "1", "").getSecond().getStatus());

        verify(DbServiceMock.mock).updateRaw("UPDATE testRestoredRecords SET isDeleted___ = ? WHERE ID IN (?)", "no", 1L);
        verify(DbServiceMock.mock).updateRaw("UPDATE testGenCollection SET isDeleted___ = ? WHERE recordID IN (?)", "no", "testRestoredRecords.1");
        verify(DbServiceMock.mock).updateRaw("UPDATE testCollection SET isDeleted___ = ? WHERE categoryID IN (?)", "no", 1L);
    }

    @Test
    public void deleteOperationMany()
    {
        whenSelectListTagsContains("FROM testtableAdmin WHERE testtableAdmin.ID = 1 LIMIT 2147483647", "1", "2", "3");

        executeOperation("testtableAdmin", "All records", "Delete", "1,2,3", "").getSecond();

        verify(DbServiceMock.mock).updateRaw("DELETE FROM testtableAdmin WHERE ID IN (?, ?, ?)", 1L, 2L, 3L);
    }

    @Test
    public void insertOperationInitValues()
    {
        Operation operation = createOperation("testtableAdmin", "All records", "Insert", "");
        assertEquals(OperationStatus.CREATE, operation.getStatus());

        Object first = generateOperation(operation, "{}").getFirst();

        assertEquals(OperationStatus.GENERATE, operation.getStatus());

        assertEquals("{'name':'','valueCol':'111'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString()));
    }

    @Test
    public void insertOperation()
    {
        Object first = generateOperation("testtable", "All records", "Insert", "",
                doubleQuotes("{'name':'test','valueCol':1}")).getFirst();
        assertEquals("{" +
                        "'values':{'name':'test','valueCol':'1'}," +
                        "'meta':{" +
                        "'/name':{'displayName':'Name','columnSize':'20'}," +
                        "'/valueCol':{'displayName':'Value Col','columnSize':'30'}}," +
                        "'order':['/name','/valueCol']}",
                oneQuotes(JsonFactory.bean(first)));

        //OperationResult execute = executeOperation(req);

        OperationResult result = executeOperation("testtable", "All records", "Insert", "",
                doubleQuotes("{'name':'test','valueCol':'1'}")).getSecond();
        assertEquals(OperationStatus.FINISHED, result.getStatus());
        assertEquals("table/testtable/All records", ((FrontendAction[])result.getDetails())[0].getValue());


        verify(DbServiceMock.mock).insertRaw("INSERT INTO testtable (name, valueCol) " +
                "VALUES (?, ?)", "test", "1");
    }

    @Test
    public void editOperationGenerate()
    {
        when(DbServiceMock.mock.select(any(), any(), any())).thenReturn(getDpsS(ImmutableMap.of(
                "name", "TestName",
                "valueCol", 1,
                "ID", 12L
        )));

        Object first = generateOperation("testtableAdmin", "All records", "Edit", "12", "{}").getFirst();

        verify(DbServiceMock.mock).select(eq("SELECT * FROM testtableAdmin WHERE ID = ?"), any(), eq(12L));

        assertEquals("{'name':'TestName','valueCol':'1'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString()));
    }

    @Test
    public void editOperationGenerateStringPrimaryKey()
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport();
        dpsHelper.addDpExcludeAutoIncrement(dps, meta.getEntity("propertyTypes"), Collections.emptyMap());
        dps.setValue("name", "TestName");
        dps.setValue("CODE", "02");
        when(DbServiceMock.mock.select(any(), any(), any())).thenReturn(dps);

        Object first = generateOperation("propertyTypes", "All records", "Edit", "01", "{}").getFirst();

        verify(DbServiceMock.mock).select(eq("SELECT * FROM propertyTypes WHERE CODE = ?"), any(), eq("01"));

        assertEquals("{'CODE':'02','name':'TestName'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString()));
    }

    @Test
    public void editSilentInvoke()
    {
        OperationResult result = executeEditWithValues("{'name':'EditName','valueCol':123}");

        verify(DbServiceMock.mock).updateRaw("UPDATE testtableAdmin SET name = ?, valueCol = ? WHERE ID = ?",
                "EditName", 123, 12L);

        assertEquals(OperationStatus.FINISHED, result.getStatus());
        assertEquals("table/testtableAdmin/All records/_selectedRows_=12", ((FrontendAction[])result.getDetails())[0].getValue());
    }

    @Test
    public void editSilentInvokeModalForm()
    {
        when(DbServiceMock.mock.select(any(), any(), any())).thenReturn(getDpsS(ImmutableMap.of(
                "name", "TestName",
                "valueCol", 12345,
                "ID", 12L
        )));

        Operation operation = createOperation("testtableAdmin", "All records", "EditModalForm", "12");
        OperationResult result = executeOperation(operation, doubleQuotes("{'name':'EditName','valueCol':123}")).getSecond();

        assertEquals(OperationStatus.FINISHED, result.getStatus());
        assertNull(result.getDetails());
        assertEquals(0, result.getTimeout());

        verify(DbServiceMock.mock).select(eq("SELECT * FROM testtableAdmin WHERE ID = ?"), any(), eq(12L));
        verify(DbServiceMock.mock).updateRaw("UPDATE testtableAdmin SET name = ?, valueCol = ? WHERE ID = ?",
                "EditName", 123, 12L);
    }

    @Test
    public void editInvokeValueNull()
    {
        executeEditWithValues("{'name':'EditName','valueCol':null}");

        verify(DbServiceMock.mock).updateRaw("UPDATE testtableAdmin SET name = ?, valueCol = ? WHERE ID = ?",
                "EditName", null, 12L);
    }

    @Test
    public void editInvokeEmptyStringToNull()
    {
        executeEditWithValues("{'name':'EditName','valueCol':''}");

        verify(DbServiceMock.mock).updateRaw("UPDATE testtableAdmin SET name = ?, valueCol = ? WHERE ID = ?",
                "EditName", null, 12L);
    }

    @Test
    public void editInvokeEmptyStringToNull2Records()
    {
        executeEditWithValues2Records("{'name':'EditName','valueCol':''}");

        verify(DbServiceMock.mock).select(eq("SELECT * FROM testtableAdmin WHERE ID = ?"), any(), eq(12L));
        verify(DbServiceMock.mock).updateRaw("UPDATE testtableAdmin SET name = ? WHERE ID IN (?, ?)",
                "EditName", 12L, 13L);
    }

    @Test
    public void editInvokeEmptyStringToNullNullable2Records()
    {
        executeEditWithValues2Records("{'name':'','valueCol':'1'}");

        verify(DbServiceMock.mock).select(eq("SELECT * FROM testtableAdmin WHERE ID = ?"), any(), eq(12L));
        verify(DbServiceMock.mock).updateRaw("UPDATE testtableAdmin SET valueCol = ? WHERE ID IN (?, ?)",
                1, 12L, 13L);
    }

    @Test
    public void editInvokeEmptyAllNulls2Records()
    {
        executeEditWithValues2Records("{'name':'','valueCol':''}");

        verify(DbServiceMock.mock).select(eq("SELECT * FROM testtableAdmin WHERE ID = ?"), any(), eq(12L));
        verifyNoMoreInteractions(DbServiceMock.mock);
    }

    private OperationResult executeEditWithValues(String values)
    {
        when(DbServiceMock.mock.select(any(), any(), any())).thenReturn(getDpsS(ImmutableMap.of(
                "name", "TestName",
                "valueCol", 12345,
                "ID", 12L
        )));

        Operation operation = createOperation("testtableAdmin", "All records", "Edit", "12");

        OperationResult result = executeOperation(operation, doubleQuotes(values)).getSecond();

        assertEquals(OperationStatus.FINISHED, result.getStatus());
        assertEquals("table/testtableAdmin/All records/_selectedRows_=12",
                ((FrontendAction[])result.getDetails())[0].getValue());

        verify(DbServiceMock.mock).select(eq("SELECT * FROM testtableAdmin WHERE ID = ?"), any(), eq(12L));

        return result;
    }

    private OperationResult executeEditWithValues2Records(String values)
    {
        Operation operation = createOperation("testtableAdmin", "All records", "Edit", "12,13");

        OperationResult result = executeOperation(operation, doubleQuotes(values)).getSecond();

        assertEquals(OperationStatus.FINISHED, result.getStatus());
        assertEquals(1, ((FrontendAction[])result.getDetails()).length);
        assertEquals("table/testtableAdmin/All records/_selectedRows_=12,13",
                ((FrontendAction[])result.getDetails())[0].getValue());

        verify(DbServiceMock.mock).select(eq("SELECT * FROM testtableAdmin WHERE ID = ?"), any(), eq(12L));

        return result;
    }

    private static void whenSelectListTagsContains(String containsSql, String... tagValues)
    {
        List<DynamicPropertySet> tagValuesList = Arrays.stream(tagValues)
                .map(tagValue -> getDps(new QRec(), ImmutableMap.of("CODE", tagValue, "Name", tagValue)))
                .collect(Collectors.toList());

        when(DbServiceMock.mock.list(contains(containsSql),
                Matchers.<ResultSetParser<DynamicPropertySet>>any(), anyVararg())).thenReturn(tagValuesList);
    }
}
