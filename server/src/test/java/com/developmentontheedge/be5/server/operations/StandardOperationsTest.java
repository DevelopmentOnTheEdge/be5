package com.developmentontheedge.be5.server.operations;

import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.query.model.beans.QRec;
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
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

        verify(DbServiceMock.mock).update("DELETE FROM testtableAdmin WHERE ID IN (?)", 1L);
        verify(DbServiceMock.mock).update("UPDATE testGenCollection SET isDeleted___ = ? WHERE recordID IN (?)", "yes", "testtableAdmin.1");
    }

    @Test
    public void deleteOperationRestoredRecords()
    {
        whenSelectListTagsContains("FROM testRestoredRecords WHERE testRestoredRecords.ID = 1 LIMIT 2147483647", "1");

        assertEquals(OperationStatus.FINISHED,
                executeOperation("testRestoredRecords", "All records", "Delete", "1", "").getSecond().getStatus());

        verify(DbServiceMock.mock).update("UPDATE testRestoredRecords SET isDeleted___ = ? WHERE ID IN (?)", "yes", 1L);
        verify(DbServiceMock.mock).update("UPDATE testGenCollection SET isDeleted___ = ? WHERE recordID IN (?)", "yes", "testRestoredRecords.1");
        verify(DbServiceMock.mock).update("UPDATE testCollection SET isDeleted___ = ? WHERE categoryID IN (?)", "yes", 1L);
    }

    @Test
    public void restoreOperationRestoredRecords()
    {
        whenSelectListTagsContains("FROM testRestoredRecords WHERE testtableAdmin.ID = 1 LIMIT 2147483647", "1");

        assertEquals(OperationStatus.FINISHED,
                executeOperation("testRestoredRecords", "All records", "Restore", "1", "").getSecond().getStatus());

        verify(DbServiceMock.mock).update("UPDATE testRestoredRecords SET isDeleted___ = ? WHERE ID IN (?)", "no", 1L);
        verify(DbServiceMock.mock).update("UPDATE testGenCollection SET isDeleted___ = ? WHERE recordID IN (?)", "no", "testRestoredRecords.1");
        verify(DbServiceMock.mock).update("UPDATE testCollection SET isDeleted___ = ? WHERE categoryID IN (?)", "no", 1L);
    }

    @Test
    public void deleteOperationMany()
    {
        whenSelectListTagsContains("FROM testtableAdmin WHERE testtableAdmin.ID = 1 LIMIT 2147483647", "1", "2", "3");

        executeOperation("testtableAdmin", "All records", "Delete", "1,2,3", "").getSecond();

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
                doubleQuotes("{'name':'test','value':1}")).getFirst();
        assertEquals("{" +
                        "'values':{'name':'test','value':'1'}," +
                        "'meta':{" +
                        "'/name':{'displayName':'name','columnSize':'20'}," +
                        "'/value':{'displayName':'value','columnSize':'30'}}," +
                        "'order':['/name','/value']}",
                oneQuotes(JsonFactory.bean(first)));

        //OperationResult execute = executeOperation(req);

        OperationResult result = executeOperation("testtable", "All records", "Insert", "",
                doubleQuotes("{'name':'test','value':'1'}")).getSecond();
        assertEquals(OperationStatus.FINISHED, result.getStatus());
        assertEquals("table/testtable/All records", ((FrontendAction[])result.getDetails())[0].getValue());


        verify(DbServiceMock.mock).insert("INSERT INTO testtable (name, value) " +
                "VALUES (?, ?)", "test", "1");
    }

    @Test
    public void editOperationGenerate()
    {
        when(DbServiceMock.mock.select(any(), any(), any())).thenReturn(getDpsS(ImmutableMap.of(
                "name", "TestName",
                "value", 1,
                "ID", 12L
        )));

        Object first = generateOperation("testtableAdmin", "All records", "Edit", "12", "{}").getFirst();

        verify(DbServiceMock.mock).select(eq("SELECT * FROM testtableAdmin WHERE ID = ?"), any(), eq(12L));

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
        when(DbServiceMock.mock.select(any(), any(), any())).thenReturn(dps);

        Object first = generateOperation("propertyTypes", "All records", "Edit", "01", "{}").getFirst();

        verify(DbServiceMock.mock).select(eq("SELECT * FROM propertyTypes WHERE CODE = ?"), any(), eq("01"));

        assertEquals("{'CODE':'02','name':'TestName'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString()));
    }

    @Test
    public void editInvoke()
    {
        OperationResult result = executeEditWithValues("{'name':'EditName','value':123}");

        verify(DbServiceMock.mock).update("UPDATE testtableAdmin SET name = ?, value = ? WHERE ID = ?",
                "EditName", 123, 12L);

        assertEquals(OperationStatus.FINISHED, result.getStatus());
        assertEquals("table/testtableAdmin/All records", ((FrontendAction[])result.getDetails())[0].getValue());
    }

    @Test
    public void editInvokeValueNull()
    {
        executeEditWithValues("{'name':'EditName','value':null}");

        verify(DbServiceMock.mock).update("UPDATE testtableAdmin SET name = ?, value = ? WHERE ID = ?",
                "EditName", null, 12L);
    }

    @Test
    public void editInvokeEmptyStringToNull()
    {
        executeEditWithValues("{'name':'EditName','value':''}");

        verify(DbServiceMock.mock).update("UPDATE testtableAdmin SET name = ?, value = ? WHERE ID = ?",
                "EditName", null, 12L);
    }

    private OperationResult executeEditWithValues(String values)
    {
        when(DbServiceMock.mock.select(any(), any(), any())).thenReturn(getDpsS(ImmutableMap.of(
                "name", "TestName",
                "value", 12345,
                "ID", 12L
        )));

        Operation operation = createOperation("testtableAdmin", "All records", "Edit", "12");

        OperationResult result = executeOperation(operation, doubleQuotes(values)).getSecond();

        assertEquals(OperationStatus.FINISHED, result.getStatus());
        assertEquals("table/testtableAdmin/All records", ((FrontendAction[])result.getDetails())[0].getValue());

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
