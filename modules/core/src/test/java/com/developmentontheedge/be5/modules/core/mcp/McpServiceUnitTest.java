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

    @Before
    public void setUp()
    {
        schemaService = mock(SchemaService.class);
        dbService = mock(DbService.class);
        mcpService = new McpService(schemaService, dbService);
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

    private Map<String, Object> callTool(String name, String argument, String value)
    {
        Map<String, Object> args = new HashMap<>();
        args.put(argument, value);
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