package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import com.developmentontheedge.be5.util.Either;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;


import java.sql.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

public class DateTimeTest extends AbstractProjectTest{

    private OperationService operationService = sqlMockInjector.get(OperationService.class);

    @Before
    public void before()
    {
        SqlServiceMock.clearMock();
    }

    @Test
    public void testOperation()
    {
        Request req = getSpyMockRecForOp("dateTime", "All records", "Insert", "0",
                jsonb.toJson(ImmutableMap.of("activeFrom","1901-02-03")));

        Either<FormPresentation, OperationResult> generate = operationService.generate(req);

        FormPresentation form = generate.getFirst();

        assertEquals("{" +
                        "'values':{'activeFrom':'1901-02-03'}," +
                        "'meta':{'/activeFrom':{'displayName':'activeFrom','type':'Date'}}," +
                        "'order':['/activeFrom']" +
                "}", oneQuotes(form.bean.toString()));

        OperationResult result = operationService.execute(req).getSecond();
        assertEquals(OperationResult.redirect("table/dateTime/All records"), result);
    }

    @Test
    public void testOperationInvoke()
    {
        operationService.execute(
                getSpyMockRecForOp("dateTime", "All records", "Insert", "0",
                        jsonb.toJson(ImmutableMap.of("activeFrom","1901-02-03"))));

        verify(SqlServiceMock.mock).insert("INSERT INTO dateTime (activeFrom) VALUES (?)",
                Date.valueOf("1901-02-03"));
    }

}