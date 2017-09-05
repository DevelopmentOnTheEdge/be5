package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.be5.test.Be5ProjectTest;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import com.developmentontheedge.be5.util.Either;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

public class OperationTest extends Be5ProjectTest
{
    @Inject private OperationService operationService;

    @Before
    public void beforeClass(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);
    }

    @After
    public void afterClass(){
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
        Request req = getSpyMockRecForOp("testtableAdmin", "All records", "TestOperation", "0",
                jsonb.toJson(ImmutableMap.of("name","testName","number", "1")));

        Either<FormPresentation, OperationResult> generate = operationService.generate(req);

        FormPresentation form = generate.getFirst();

        assertEquals("TestOperation", form.title);

        assertEquals("{" +
                        "'values':{" +
                            "'name':'testName'," +
                            "'number':'1'}," +
                        "'meta':{" +
                            "'/name':{'displayName':'Name'}," +
                            "'/number':{'displayName':'Number','type':'Long'}}," +
                        "'order':['/name','/number']}",
                oneQuotes(form.bean.toString()));

        OperationResult result = operationService.execute(req).getSecond();
        assertEquals(OperationResult.redirect("table/testtableAdmin/All records"), result);
    }

    @Test
    public void testOperationParameters()
    {
        Either<FormPresentation, OperationResult> generate = operationService.generate(
                getSpyMockRecForOp("testtableAdmin", "All records", "TestOperation", "0","{}"));

        assertEquals("{'name':'','number':'0'}", oneQuotes(generate.getFirst().getBean().getJsonObject("values").toString()));
    }

    @Test
    public void testReloadOnChange()
    {
        Either<FormPresentation, OperationResult> generate = operationService.generate(
                getSpyMockRecForOp("testtableAdmin", "All records", "TestOperation", "0",
                        jsonb.toJson(ImmutableMap.of(
                                "name", "",
                                "number", "0",
                                OperationSupport.reloadControl, "name"))));

        assertEquals("{'name':'','number':0}", oneQuotes(generate.getFirst().getBean().getJsonObject("values").toString()));
    }

    @Test
    public void testReloadOnChangeError()
    {
        Request spyMockRecForOp = getSpyMockRecForOp("testtableAdmin", "All records", "TestOperation", "0",
                jsonb.toJson(ImmutableMap.of(
                        "name", "testName",
                        "number", "ab",
                        OperationSupport.reloadControl, "name")));

        assertNotNull(operationService.generate(spyMockRecForOp).getFirst());

        Either<FormPresentation, OperationResult> result = operationService.generate(spyMockRecForOp);

        assertNotNull(result.getFirst());

        assertEquals("error", result.getFirst().bean.getJsonObject("meta").getJsonObject("/number").getString("status"));
        assertEquals("Error, value must be a java.lang.Long",
                result.getFirst().bean.getJsonObject("meta").getJsonObject("/number").getString("message"));
    }

    @Test
    public void testOperationParametersErrorInvoke()
    {
        Request spyMockRecForOp = getSpyMockRecForOp("testtableAdmin", "All records", "TestOperation", "0",
                doubleQuotes("{'name':'testName','number':'ab'}"));

        assertNotNull(operationService.generate(spyMockRecForOp).getFirst());

        Either<FormPresentation, OperationResult> result = operationService.execute(spyMockRecForOp);

        assertNotNull(result.getFirst());

        assertEquals("error", result.getFirst().bean.getJsonObject("meta").getJsonObject("/number").getString("status"));
        assertEquals("Error, value must be a java.lang.Long",
                result.getFirst().bean.getJsonObject("meta").getJsonObject("/number").getString("message"));
    }

    @Test
    public void testOperationInvoke()
    {
        operationService.execute(
                getSpyMockRecForOp("testtableAdmin", "All records", "TestOperation", "0",
                        doubleQuotes("{'name':'testName','number':3}")));

        verify(SqlServiceMock.mock).insert("INSERT INTO testtableAdmin (name, number) " +
                "VALUES (?, ?)", "testName", 3L);
    }

    @Test
    public void testPropertyInvokeInit()
    {
        Either<FormPresentation, OperationResult> generate = operationService.generate(
                getSpyMockRecForOp("testtableAdmin", "All records", "TestOperationProperty", "0", "{}"));

        assertEquals("{" +
                        "'simple':''," +
                        "'simpleNumber':''," +
                        "'getOrDefault':'defaultValue'," +
                        "'getOrDefaultNumber':'3'}",
                oneQuotes(generate.getFirst().getBean().getJsonObject("values").toString()));
    }

    @Test
    public void testManualAndAutomaticSettingOfParameterValues()
    {
        Either<FormPresentation, OperationResult> generate = operationService.generate(
                getSpyMockRecForOp("testtableAdmin", "All records", "TestOperationProperty", "0",
                        jsonb.toJson(ImmutableMap.of(
                                "simple", "testName",
                                "simpleNumber", "1",
                                "getOrDefault", "testName2",
                                "getOrDefaultNumber", "2",
                                OperationSupport.reloadControl, "name"))));

        assertEquals("{" +
                        "'simple':'testName'," +
                        "'simpleNumber':1," +
                        "'getOrDefault':'testName2'," +
                        "'getOrDefaultNumber':2}",
                oneQuotes(generate.getFirst().getBean().getJsonObject("values").toString()));

        assertFalse(generate.getFirst().toString().contains("error"));
    }

}