package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import com.developmentontheedge.be5.util.Either;
import com.google.common.collect.ImmutableMap;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


import java.sql.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

public class DateTimeTest extends AbstractProjectTest
{
    @Inject private  OperationService operationService;

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
    public void invoke()
    {
        operationService.execute(
                getSpyMockRecForOp("dateTime", "All records", "Insert", "0",
                        jsonb.toJson(ImmutableMap.of("activeFrom","1901-02-03"))));

        verify(SqlServiceMock.mock).insert("INSERT INTO dateTime (activeFrom) VALUES (?)",
                Date.valueOf("1901-02-03"));
    }

    @Test
    public void invokeDefaultValue()
    {
        Either<FormPresentation, OperationResult> execute = operationService.execute(
                getSpyMockRecForOp("dateTime", "All records", "Insert", "0",
                        "{}"));

        verify(SqlServiceMock.mock).insert("INSERT INTO dateTime (activeFrom) VALUES (?)",
                Date.valueOf("1900-01-01"));
    }

}