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
        assertEquals("2024-11-05", result.get("protocolVersion"));
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