package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.be5.test.SqlMockOperationTest;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.beans.json.JsonFactory;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;


import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;


public class OperationTest extends SqlMockOperationTest
{
    @Test
    public void testOperation()
    {
        Either<Object, OperationResult> generate = generateOperation("testtableAdmin", "All records", "TestOperation", "0",
                ImmutableMap.of("name","testName","number", "1"));

        Object parameters = generate.getFirst();

        assertEquals("{" +
                        "'values':{" +
                            "'name':'testName'," +
                            "'number':'1'}," +
                        "'meta':{" +
                            "'/name':{'displayName':'Name'}," +
                            "'/number':{'displayName':'Number','type':'Long'}}," +
                        "'order':['/name','/number']}",
                oneQuotes(JsonFactory.bean(parameters)));

        OperationResult result = executeOperation("testtableAdmin", "All records", "TestOperation", "0",
                ImmutableMap.of("name","testName","number", "1")).getSecond();
        assertEquals(OperationResult.redirect("table/testtableAdmin/All records"), result);
    }

    @Test
    public void testOperationParameters()
    {
        Either<Object, OperationResult> generate = generateOperation("testtableAdmin", "All records", "TestOperation", "0","{}");

        assertEquals("{'name':'','number':'0'}", oneQuotes(JsonFactory.bean(generate.getFirst()).getJsonObject("values").toString()));
    }

    @Test
    public void testReloadOnChange()
    {
        Either<Object, OperationResult> generate = generateOperation(
                "testtableAdmin", "All records", "TestOperation", "0",
                        ImmutableMap.of(
                                "name", "test",
                                "number", "0",
                                OperationSupport.reloadControl, "name"));

        assertEquals("{'name':'test','number':0}", oneQuotes(JsonFactory.bean(generate.getFirst()).getJsonObject("values").toString()));
    }

    @Test
    public void testReloadOnChangeError()
    {
        Either<Object, OperationResult> result = generateOperation("testtableAdmin", "All records", "TestOperation", "0",
                ImmutableMap.of(
                        "name", "testName",
                        "number", "ab",
                        OperationSupport.reloadControl, "name"));

        assertNotNull(result.getFirst());

        assertNotNull(result.getFirst());

        assertEquals("error", JsonFactory.bean(result.getFirst()).getJsonObject("meta").getJsonObject("/number").getString("status"));
        assertEquals("Здесь должно быть целое число.",
                JsonFactory.bean(result.getFirst()).getJsonObject("meta").getJsonObject("/number").getString("message"));
    }

    @Test
    public void testOperationParametersErrorInvoke()
    {
        assertNotNull(generateOperation("testtableAdmin", "All records", "TestOperation", "0",
                doubleQuotes("{'name':'testName','number':'ab'}")).getFirst());

        Either<Object, OperationResult> result = executeOperation("testtableAdmin", "All records", "TestOperation", "0",
                doubleQuotes("{'name':'testName','number':'ab'}"));

        assertNotNull(result.getFirst());

        assertEquals("error", JsonFactory.bean(result.getFirst()).getJsonObject("meta").getJsonObject("/number").getString("status"));
        assertEquals("Здесь должно быть целое число.",
                JsonFactory.bean(result.getFirst()).getJsonObject("meta").getJsonObject("/number").getString("message"));
    }

    @Test
    public void testOperationInvoke()
    {
        executeOperation("testtableAdmin", "All records", "TestOperation", "0",
                                                    doubleQuotes("{'name':'testName','number':3}"));

        verify(SqlServiceMock.mock).insert("INSERT INTO testtableAdmin (name, number) " +
                "VALUES (?, ?)", "testName", 3L);
    }

    @Test
    public void testPropertyInvokeInit()
    {
        Either<Object, OperationResult> generate = generateOperation(
                "testtableAdmin", "All records", "TestOperationProperty", "0", "{}");

        assertEquals("{" +
                        "'simple':''," +
                        "'simpleNumber':''," +
                        "'getOrDefault':'defaultValue'," +
                        "'getOrDefaultNumber':'3'}",
                oneQuotes(JsonFactory.bean(generate.getFirst()).getJsonObject("values").toString()));
    }

    @Test
    public void testManualAndAutomaticSettingOfParameterValues()
    {
        Either<Object, OperationResult> generate = generateOperation(
                "testtableAdmin", "All records", "TestOperationProperty", "0",
                        ImmutableMap.of(
                                "simple", "testName",
                                "simpleNumber", "1",
                                "getOrDefault", "testName2",
                                "getOrDefaultNumber", "2",
                                OperationSupport.reloadControl, "name"));

        assertEquals("{" +
                        "'simple':'testName'," +
                        "'simpleNumber':1," +
                        "'getOrDefault':'testName2'," +
                        "'getOrDefaultNumber':2}",
                oneQuotes(JsonFactory.bean(generate.getFirst()).getJsonObject("values").toString()));

        assertFalse(generate.getFirst().toString().contains("error"));
    }

    @Test
    public void testOperationInvokeNullInsteadEmptyString()
    {
        executeOperation("testTags", "All records", "Insert", "",
                doubleQuotes("{'CODE':'01','referenceTest':'','payable':'yes','admlevel':'Regional','testLong':''}")).getSecond();

        verify(SqlServiceMock.mock).insert("INSERT INTO testTags (referenceTest, CODE, payable, admlevel, testLong) VALUES (?, ?, ?, ?, ?)",
                null, "01", "yes", "Regional", null
        );
    }

    @Test
    public void testOperationInvokeNullInsteadEmptyStringCustomOp()
    {
        OperationResult second = executeOperation(
                "testTags", "All records", "OperationWithCanBeNull", "",
                        doubleQuotes("{'CODE':'01','referenceTest':'','testLong':''}")).getSecond();

        verify(SqlServiceMock.mock).insert("INSERT INTO testTags (CODE, referenceTest, testLong) VALUES (?, ?, ?)",
                "01", null, null
        );
    }

    @Test
    public void testOperationInvokeNullInsteadEmptyStringCustomOpSpace()
    {
        OperationResult second = executeOperation(
                "testTags", "All records", "OperationWithCanBeNull", "",
                        doubleQuotes("{'CODE':'01','referenceTest':' ','testLong':''}")).getSecond();

        verify(SqlServiceMock.mock).insert("INSERT INTO testTags (CODE, referenceTest, testLong) VALUES (?, ?, ?)",
                "01", " ", null
        );
    }

    @Test
    public void testOperationInvokeNullInsteadEmptyStringCustomOpSpaceOnLong()
    {
        Object first = executeOperation(
                "testTags", "All records", "OperationWithCanBeNull", "",
                        doubleQuotes("{'CODE':'01','referenceTest':'','testLong':' '}")).getFirst();

        assertEquals("error", JsonFactory.bean(first).getJsonObject("meta").getJsonObject("/testLong").getString("status")) ;
    }

}