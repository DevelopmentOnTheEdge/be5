package com.developmentontheedge.be5.operation

import com.developmentontheedge.be5.base.FrontendConstants
import com.developmentontheedge.be5.operation.OperationsSqlMockProjectTest
import com.developmentontheedge.be5.operation.model.Operation
import com.developmentontheedge.be5.operation.model.OperationContext
import com.developmentontheedge.be5.operation.model.OperationResult
import com.developmentontheedge.be5.operation.util.Either
import com.developmentontheedge.be5.test.BaseTestUtils
import com.developmentontheedge.be5.test.mocks.DbServiceMock
import com.developmentontheedge.beans.json.JsonFactory
import com.google.common.collect.ImmutableMap
import org.junit.Ignore
import org.junit.Test

import static com.developmentontheedge.be5.base.FrontendConstants.RELOAD_CONTROL_NAME
import static org.junit.Assert.*
import static org.mockito.Mockito.verify


class OperationTest extends OperationsSqlMockProjectTest
{
    @Test
    void testOperation()
    {
        Either<Object, OperationResult> generate = generateOperation("testtableAdmin", "All records", "TestOperation", "",
                ImmutableMap.of("name", "testName", "value", "1"))

        Object parameters = generate.getFirst()

        assertEquals("{" +
                "'values':{" +
                "'name':'testName'," +
                "'value':'1'}," +
                "'meta':{" +
                "'/name':{'displayName':'Name'}," +
                "'/value':{'displayName':'Value','type':'Long'}}," +
                "'order':['/name','/value']}",
                oneQuotes(JsonFactory.bean(parameters)))

        OperationResult result = executeOperation("testtableAdmin", "All records", "TestOperation", "",
                ImmutableMap.of("name", "testName", "value", "1")).getSecond()
        assertEquals(OperationResult.redirect("table/testtableAdmin/All records"), result)
    }

    @Test
    void withOperationParams()
    {
        OperationResult result = executeOperation(
                createOperation("testtableAdmin", "All records", "TestOperation", ["name": "foo"]),
                ImmutableMap.of("name", "testName", "value", "1")).getSecond()
        assertEquals(OperationResult.redirect("table/testtableAdmin/All records/name=foo"), result)
    }

    @Test
    void testRemoveSelectedRows()
    {
        OperationResult result = executeOperation(createOperation("testtableAdmin", "All records", "TestOperation1",
                 ImmutableMap.of("name", "foo", OperationConstants.SELECTED_ROWS, "1")), [:]).getSecond()
        assertEquals(OperationResult.redirect("table/testtableAdmin/All records/name=foo"), result)
    }

    @Test
    void testOperationParameters()
    {
        Either<Object, OperationResult> generate = generateOperation("testtableAdmin", "All records", "TestOperation", "0", [:])

        assertEquals("{'name':'','value':'0'}", oneQuotes(JsonFactory.bean(generate.getFirst()).getJsonObject("values").toString()))
    }

    @Test
    void testReloadOnChange()
    {
        def operation = createOperation("testtableAdmin", "All records", "TestOperation", "0")
        Either<Object, OperationResult> generate = generateOperation(operation,
                ImmutableMap.of(
                        "name", "test",
                        "value", "0",
                        RELOAD_CONTROL_NAME, "/name"))

        assertEquals("{'name':'test','value':'0'}", oneQuotes(JsonFactory.bean(generate.getFirst()).getJsonObject("values").toString()))
    }

    @Test
    void testReloadOnChangeError()
    {
        Either<Object, OperationResult> result = generateOperation("testtableAdmin", "All records", "TestOperation", "0",
                ImmutableMap.of(
                        "name", "testName",
                        "value", "ab",
                        RELOAD_CONTROL_NAME, "/value"))

        assertNotNull(result.getFirst())

        assertNotNull(result.getFirst())

        assertEquals("error", JsonFactory.bean(result.getFirst()).getJsonObject("meta").getJsonObject("/value").getString("status"))
        assertEquals("Здесь должно быть целое число.",
                JsonFactory.bean(result.getFirst()).getJsonObject("meta").getJsonObject("/value").getString("message"))
    }

    @Test
    void testOperationParametersErrorInvoke()
    {
        assertNotNull(generateOperation("testtableAdmin", "All records", "TestOperation", "0",
                ['name': 'testName', 'value': 'ab']).getFirst())

        Either<Object, OperationResult> result = executeOperation("testtableAdmin", "All records", "TestOperation", "0",
                ['name': 'testName', 'value': 'ab'])

        assertNotNull(result.getFirst())

        assertEquals("error", JsonFactory.bean(result.getFirst()).getJsonObject("meta").getJsonObject("/value").getString("status"))
        assertEquals("Здесь должно быть целое число.",
                JsonFactory.bean(result.getFirst()).getJsonObject("meta").getJsonObject("/value").getString("message"))
    }

    @Test
    void testOperationInvoke()
    {
        executeOperation("testtableAdmin", "All records", "TestOperation", "0",
                ['name': 'testName', 'value': 3L])

        verify(DbServiceMock.mock).insert("INSERT INTO testtableAdmin (name, value) " +
                "VALUES (?, ?)", "testName", 3L)
    }

    @Test
    void testPropertyInvokeInit()
    {
        Either<Object, OperationResult> generate = generateOperation(
                "testtableAdmin", "All records", "TestOperationProperty", "0", [:])

        assertEquals("{" +
                "'getOrDefault':'defaultValue'," +
                "'getOrDefaultNumber':'3'}",
                oneQuotes(JsonFactory.bean(generate.getFirst()).getJsonObject("values").toString()))
    }

    @Test
    void testManualAndAutomaticSettingOfParameterValues()
    {
        Either<Object, OperationResult> generate = generateOperation(
                "testtableAdmin", "All records", "TestOperationProperty", "0", [
                "getOrDefault"      : "testName2",
                "getOrDefaultNumber": "2",
                (RELOAD_CONTROL_NAME) : "/getOrDefault"])

        assertEquals("{" +
                "'getOrDefault':'testName2'," +
                "'getOrDefaultNumber':'2'}",
                oneQuotes(JsonFactory.bean(generate.getFirst()).getJsonObject("values").toString()))

        assertFalse(generate.getFirst().toString().contains("error"))
    }

    @Test
    @Ignore
    void testOperationInvokeNullInsteadEmptyString()
    {
        executeOperation("testTags", "All records", "Insert", "",
                ['CODE': '01', 'referenceTest': '', 'payable': 'yes', 'admlevel': 'Regional']).getSecond()

        verify(DbServiceMock.mock).insert("INSERT INTO testTags (CODE, payable, admlevel) VALUES (?, ?, ?)",
                "01", "yes", "Regional"
        )
    }

    @Test
    void testOperationInvokeNullInsteadEmptyStringCustomOp()
    {
        OperationResult second = executeOperation(
                "testTags", "All records", "OperationWithCanBeNull", "",
                ['CODE': '01', 'referenceTest': '', 'testLong': '']).getSecond()

        assertEquals(null, second.getMessage())
    }

    @Test
    void testOperationInvokeNullInsteadEmptyStringCustomOpSpace()
    {
        OperationResult second = executeOperation(
                "testTags", "All records", "OperationWithCanBeNull", "",
                ['CODE': '01', 'referenceTest': ' ', 'testLong': '']).getSecond()

        assertEquals(' ', second.getMessage())
    }

    @Test
    void testOperationInvokeNullInsteadEmptyStringCustomOpSpaceOnLong()
    {
        Object first = executeOperation(
                "testTags", "All records", "OperationWithCanBeNull", "",
                ['CODE': '01', 'referenceTest': '', 'testLong': ' ']).getFirst()

        assertEquals("error", JsonFactory.bean(first).getJsonObject("meta").getJsonObject("/testLong").getString("status"))
    }

}
