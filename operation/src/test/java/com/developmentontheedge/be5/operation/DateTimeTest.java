package com.developmentontheedge.be5.operation;

import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.operation.model.OperationResult;
import com.developmentontheedge.be5.operation.util.Either;
import com.developmentontheedge.be5.test.BaseTestUtils;
import com.developmentontheedge.be5.test.mocks.DbServiceMock;
import com.developmentontheedge.beans.json.JsonFactory;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Date;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;


public class DateTimeTest extends OperationsSqlMockProjectTest
{
    @Before
    public void setUp()
    {
        DbServiceMock.clearMock();
    }

    @Test
    public void testOperation()
    {
        Operation operation = createOperation("testtable", "All records", "DateTimeTestOperation", "0");
        ImmutableMap<String, Object> values = ImmutableMap.of("activeFrom", "1901-02-03");

        Either<Object, OperationResult> generate = generateOperation(operation, values);

        Object parameters = generate.getFirst();

        Assert.assertEquals("{" +
                "'values':{'activeFrom':'1901-02-03'}," +
                "'meta':{'/activeFrom':{'displayName':'activeFrom','type':'Date'}}," +
                "'order':['/activeFrom']" +
                "}", BaseTestUtils.oneQuotes(JsonFactory.bean(parameters)));

        OperationResult result = executeOperation(operation, values).getSecond();
        assertEquals(OperationResult.redirect("table/testtable/All records"), result);
    }

    @Test
    public void invoke()
    {
        executeOperation("testtable", "All records", "DateTimeTestOperation", "0", ImmutableMap.of("activeFrom", "1901-02-03"));

        verify(DbServiceMock.mock).insert("INSERT INTO testtable (activeFrom) VALUES (?)",
                Date.valueOf("1901-02-03"));
    }

    @Test
    public void invokeEmptyValue()
    {
        Object first = executeOperation("testtable", "All records", "DateTimeTestOperation", "0",
                ImmutableMap.of("activeFrom", "")).getFirst();

        assertEquals("Это поле должно быть заполнено.",
                JsonFactory.bean(first).getJsonObject("meta").getJsonObject("/activeFrom").getString("message"));
    }

    @Test
    public void invokeDefaultValue()
    {
        executeOperation("testtable", "All records", "DateTimeTestOperation", "0", Collections.emptyMap());

        verify(DbServiceMock.mock).insert("INSERT INTO testtable (activeFrom) VALUES (?)",
                Date.valueOf("1900-01-01"));
    }

}