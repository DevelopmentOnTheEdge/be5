package com.developmentontheedge.be5.server.operations;

import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.operation.model.OperationResult;
import com.developmentontheedge.be5.server.test.SqlMockOperationTest;
import com.developmentontheedge.be5.test.mocks.DbServiceMock;
import com.developmentontheedge.be5.server.util.Either;
import com.developmentontheedge.beans.json.JsonFactory;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.sql.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

public class DateTimeTest extends SqlMockOperationTest
{
    @Test
    public void testOperation()
    {
        Operation operation = createOperation("dateTime", "All records", "Insert", "0");
        ImmutableMap<String, Object> values = ImmutableMap.of("activeFrom", "1901-02-03");

        Either<Object, OperationResult> generate = generateOperation(operation, values);

        Object parameters = generate.getFirst();

        assertEquals("{" +
                        "'values':{'activeFrom':'1901-02-03'}," +
                        "'meta':{'/activeFrom':{'displayName':'activeFrom','type':'Date'}}," +
                        "'order':['/activeFrom']" +
                "}", oneQuotes(JsonFactory.bean(parameters)));

        OperationResult result = executeOperation(operation, values).getSecond();
        assertEquals(OperationResult.redirect("table/dateTime/All records"), result);
    }

    @Test
    public void invoke()
    {
        executeOperation("dateTime", "All records", "Insert", "0", ImmutableMap.of("activeFrom","1901-02-03"));

        verify(DbServiceMock.mock).insert("INSERT INTO dateTime (activeFrom) VALUES (?)",
                Date.valueOf("1901-02-03"));
    }

    @Test
    public void invokeEmptyValue()
    {
        Object first = executeOperation("dateTime", "All records", "Insert", "0",
                        jsonb.toJson(ImmutableMap.of("activeFrom", ""))).getFirst();

        assertEquals("Это поле должно быть заполнено.",
                JsonFactory.bean(first).getJsonObject("meta").getJsonObject("/activeFrom").getString("message"));
    }

    @Test
    public void invokeDefaultValue()
    {
        executeOperation("dateTime", "All records", "Insert", "0", "{}");

        verify(DbServiceMock.mock).insert("INSERT INTO dateTime (activeFrom) VALUES (?)",
                Date.valueOf("1900-01-01"));
    }

}