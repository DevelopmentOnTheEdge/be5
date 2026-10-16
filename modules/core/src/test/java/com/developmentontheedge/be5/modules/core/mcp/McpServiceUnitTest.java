package com.developmentontheedge.be5.modules.core.mcp;

import com.developmentontheedge.be5.database.DbService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class McpServiceUnitTest
{
    private McpService mcpService;
    private SchemaService schemaService;
    private DbService dbService;
    private QueryOperationService queryOperationService;

    @Before
    public void setUp()
    {
        schemaService = mock(SchemaService.class);
        dbService = mock(DbService.class);
        queryOperationService = mock(QueryOperationService.class);
        mcpService = new McpService(schemaService, dbService, queryOperationService);
    }

    @Test
    public void testInitialize()
    {
        Map<String, Object> request = createJsonRpcRequest("initialize", null);
        Map<String, Object> response = mcpService.handleRequest(request);

        assertNotNull(response.get("result"));
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.get("result");
        assertEquals("2025-11-25", result.get("protocolVersion"));
        assertEquals(McpService.INSTRUCTIONS, result.get("instructions"));
    }

    @Test
    public void testInitializeNegotiatesVersion()
    {
        assertEquals("2025-06-18", initializeWith("2025-06-18"));
        assertEquals("2024-11-05", initializeWith("2024-11-05"));
        assertEquals("2025-11-25", initializeWith("1999-01-01"));
    }

    @SuppressWarnings("unchecked")
    private Object initializeWith(String protocolVersion)
    {
        Map<String, Object> params = new HashMap<>();
        params.put("protocolVersion", protocolVersion);
        Map<String, Object> response = mcpService.handleRequest(createJsonRpcRequest("initialize", params));
        return ((Map<String, Object>) response.get("result")).get("protocolVersion");
    }

    @Test
    public void testNotificationIsNotAnswered()
    {
        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("method", "notifications/initialized");

        assertNull(mcpService.handleRequest(request));
    }

    @Test
    public void testPing()
    {
        Map<String, Object> response = mcpService.handleRequest(createJsonRpcRequest("ping", null));

        assertEquals(new HashMap<>(), response.get("result"));
    }

    @Test
    public void testNumericIdIsKept()
    {
        Map<String, Object> request = createJsonRpcRequest("ping", null);
        request.put("id", new java.math.BigDecimal(7));

        assertEquals(new java.math.BigDecimal(7), mcpService.handleRequest(request).get("id"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testToolFailureIsErrorResult()
    {
        when(schemaService.getTableInfo(any())).thenThrow(new RuntimeException("no such table"));

        Map<String, Object> result = (Map<String, Object>) callTool("get_table_info", "tableName", "x").get("result");

        assertEquals(true, result.get("isError"));
        List<Map<String, Object>> content = (List<Map<String, Object>>) result.get("content");
        assertEquals("no such table", content.get(0).get("text"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMissingToolArgumentIsErrorResult()
    {
        Map<String, Object> result = (Map<String, Object>) callTool("get_table_info", "other", "x").get("result");

        assertEquals(true, result.get("isError"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testOnlySelectIsExecuted()
    {
        Map<String, Object> result = (Map<String, Object>) callTool("execute_sql", "sql", "DELETE FROM users")
                .get("result");

        assertEquals(true, result.get("isError"));
        verify(dbService, never()).execute(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testResourceRead()
    {
        when(schemaService.getEntities()).thenReturn(new ArrayList<>());
        Map<String, Object> params = new HashMap<>();
        params.put("uri", "schema://entities");

        Map<String, Object> response = mcpService.handleRequest(createJsonRpcRequest("resources/read", params));

        Map<String, Object> result = (Map<String, Object>) response.get("result");
        List<Map<String, Object>> contents = (List<Map<String, Object>>) result.get("contents");
        assertEquals("schema://entities", contents.get(0).get("uri"));
        assertEquals("[]", contents.get(0).get("text"));

        params.put("uri", "schema://unknown");
        assertNotNull(mcpService.handleRequest(createJsonRpcRequest("resources/read", params)).get("error"));
    }

    @Test
    public void testResponsesAreSerializable()
    {
        javax.json.bind.Jsonb jsonb = javax.json.bind.JsonbBuilder.create();
        Map<String, Object> request = createJsonRpcRequest("initialize", null);
        request.put("id", new java.math.BigDecimal(7));

        String initialize = jsonb.toJson(mcpService.handleRequest(request));
        assertTrue(initialize, initialize.contains("\"id\":7"));
        assertTrue(initialize, initialize.contains("\"tools\":{\"listChanged\":false}"));

        String toolsList = jsonb.toJson(mcpService.handleRequest(createJsonRpcRequest("tools/list", null)));
        assertTrue(toolsList, toolsList.contains("\"annotations\":{\"readOnlyHint\":true}"));
        assertTrue(toolsList, toolsList.contains("\"required\":[\"sql\"]"));

        String templates = jsonb.toJson(mcpService.handleRequest(createJsonRpcRequest("resources/templates/list", null)));
        assertTrue(templates, templates.contains("\"resourceTemplates\":[]"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testQueryAndOperationTools()
    {
        Map<String, Object> response = mcpService.handleRequest(createJsonRpcRequest("tools/list", null));
        Map<String, Map<String, Object>> tools = new HashMap<>();
        for (Map<String, Object> tool : (List<Map<String, Object>>) ((Map<String, Object>) response.get("result"))
                .get("tools"))
        {
            tools.put((String) tool.get("name"), tool);
        }

        for (String name : new String[]{"list_queries", "describe_query", "execute_query", "get_operation_parameters"})
        {
            assertEquals(name, true, ((Map<String, Object>) tools.get(name).get("annotations")).get("readOnlyHint"));
        }
        Map<String, Object> annotations = (Map<String, Object>) tools.get("execute_operation").get("annotations");
        assertEquals(false, annotations.get("readOnlyHint"));
        assertEquals(true, annotations.get("destructiveHint"));
        Map<String, Object> uploadAnnotations = (Map<String, Object>) tools.get("upload_file").get("annotations");
        assertEquals(false, uploadAnnotations.get("readOnlyHint"));
        assertEquals(false, uploadAnnotations.get("destructiveHint"));

        Map<String, Object> schema = (Map<String, Object>) tools.get("execute_operation").get("inputSchema");
        assertEquals(java.util.Arrays.asList("entityName", "queryName", "operationName"), schema.get("required"));
        Map<String, Object> selectedRows = (Map<String, Object>) ((Map<String, Object>) schema.get("properties"))
                .get("selectedRows");
        assertEquals("array", selectedRows.get("type"));
        assertNull(((Map<String, Object>) tools.get("list_queries").get("inputSchema")).get("required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteQueryArguments()
    {
        Map<String, Object> rows = new HashMap<>();
        rows.put("rows", new ArrayList<>());
        when(queryOperationService.executeQuery(any(), any(), any(), anyInt(), anyInt())).thenReturn(rows);
        Map<String, Object> filter = new HashMap<>();
        filter.put("status", "open");
        Map<String, Object> args = new HashMap<>();
        args.put("entityName", "orders");
        args.put("queryName", "All records");
        args.put("params", filter);
        args.put("offset", new java.math.BigDecimal(40));
        args.put("limit", "10");

        Map<String, Object> result = (Map<String, Object>) callTool("execute_query", args).get("result");

        assertEquals(false, result.get("isError"));
        verify(queryOperationService).executeQuery("orders", "All records", filter, 40, 10);
        assertEquals("{\"rows\":[]}",
                ((List<Map<String, Object>>) result.get("content")).get(0).get("text"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteOperationArguments()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("entityName", "orders");
        args.put("queryName", "All records");
        args.put("operationName", "Cancel");
        args.put("selectedRows", java.util.Arrays.asList(new java.math.BigDecimal(12), "13"));
        args.put("values", "{\"reason\": \"duplicate\"}");

        callTool("execute_operation", args);

        Map<String, Object> values = new HashMap<>();
        values.put("reason", "duplicate");
        verify(queryOperationService).executeOperation("orders", "All records", "Cancel",
                java.util.Collections.emptyMap(), java.util.Arrays.asList("12", "13"), values);

        args.remove("operationName");
        Map<String, Object> result = (Map<String, Object>) callTool("execute_operation", args).get("result");
        assertEquals(true, result.get("isError"));
        assertEquals("Missing argument: operationName",
                ((List<Map<String, Object>>) result.get("content")).get(0).get("text"));
    }

    @Test
    public void testUploadFile()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("purpose", "Contract");

        callTool("upload_file", args);

        verify(queryOperationService).createUpload("Contract");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testListQueriesWithoutEntity()
    {
        when(queryOperationService.listQueries(null)).thenReturn(new ArrayList<>());

        Map<String, Object> result = (Map<String, Object>) callTool("list_queries", new HashMap<>()).get("result");

        assertEquals(false, result.get("isError"));
        verify(queryOperationService).listQueries(null);
    }

    private Map<String, Object> callTool(String name, String argument, String value)
    {
        Map<String, Object> args = new HashMap<>();
        args.put(argument, value);
        return callTool(name, args);
    }

    private Map<String, Object> callTool(String name, Map<String, Object> args)
    {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("arguments", args);
        return mcpService.handleRequest(createJsonRpcRequest("tools/call", params));
    }

    @Test
    public void testToolsList()
    {
        Map<String, Object> request = createJsonRpcRequest("tools/list", null);
        Map<String, Object> response = mcpService.handleRequest(request);

        assertNotNull(response.get("result"));
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.get("result");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tools = (List<Map<String, Object>>) result.get("tools");
        assertTrue(tools.size() >= 5);
    }

    @Test
    public void testListEntitiesTool()
    {
        when(schemaService.getEntities()).thenReturn(new ArrayList<>());

        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("id", "1");
        request.put("method", "tools/call");
        Map<String, Object> params = new HashMap<>();
        params.put("name", "list_entities");
        params.put("arguments", new HashMap<>());
        request.put("params", params);

        Map<String, Object> response = mcpService.handleRequest(request);

        assertNotNull(response.get("result"));
    }

    @Test
    public void testInvalidMethod()
    {
        Map<String, Object> request = createJsonRpcRequest("invalid_method", null);
        Map<String, Object> response = mcpService.handleRequest(request);

        assertNotNull(response.get("error"));
        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) response.get("error");
        assertEquals(-32601, error.get("code"));
    }

    @Test
    public void testInvalidTool()
    {
        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("id", "1");
        request.put("method", "tools/call");
        Map<String, Object> params = new HashMap<>();
        params.put("name", "nonexistent_tool");
        params.put("arguments", new HashMap<>());
        request.put("params", params);

        Map<String, Object> response = mcpService.handleRequest(request);

        assertNotNull(response.get("error"));
    }

    @Test
    public void testResourcesList()
    {
        Map<String, Object> request = createJsonRpcRequest("resources/list", null);
        Map<String, Object> response = mcpService.handleRequest(request);

        assertNotNull(response.get("result"));
    }

    @Test
    public void testResponseId()
    {
        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("id", "test-id");
        request.put("method", "initialize");
        request.put("params", new HashMap<>());

        Map<String, Object> response = mcpService.handleRequest(request);

        assertEquals("test-id", response.get("id"));
    }

    @Test
    public void testListDbColumns()
    {
        when(schemaService.getDatabaseColumns()).thenReturn(new ArrayList<>());

        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("id", "1");
        request.put("method", "tools/call");
        Map<String, Object> params = new HashMap<>();
        params.put("name", "list_db_columns");
        params.put("arguments", new HashMap<>());
        request.put("params", params);

        Map<String, Object> response = mcpService.handleRequest(request);

        assertNotNull(response.get("result"));
    }

    @Test
    public void testGetTableInfo()
    {
        when(schemaService.getTableInfo(any())).thenReturn(new HashMap<>());

        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("id", "1");
        request.put("method", "tools/call");
        Map<String, Object> params = new HashMap<>();
        params.put("name", "get_table_info");
        Map<String, Object> args = new HashMap<>();
        args.put("tableName", "testtable");
        params.put("arguments", args);
        request.put("params", params);

        Map<String, Object> response = mcpService.handleRequest(request);

        assertNotNull(response.get("result"));
    }

    private Map<String, Object> createJsonRpcRequest(String method, Map<String, Object> params)
    {
        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("id", "1");
        request.put("method", method);
        if (params != null)
        {
            request.put("params", params);
        }
        return request;
    }
}