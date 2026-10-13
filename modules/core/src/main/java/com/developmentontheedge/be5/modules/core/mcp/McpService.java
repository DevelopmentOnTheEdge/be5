package com.developmentontheedge.be5.modules.core.mcp;

import com.developmentontheedge.be5.database.DbService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class McpService
{
    private static final Logger log = Logger.getLogger(McpService.class.getName());
    private static final Jsonb JSONB = JsonbBuilder.create();

    /** Supported protocol versions, the latest first. */
    static final List<String> PROTOCOL_VERSIONS = Collections.unmodifiableList(Arrays.asList(
            "2025-11-25", "2025-06-18", "2025-03-26", "2024-11-05"));

    private static final int METHOD_NOT_FOUND = -32601;
    private static final int INVALID_PARAMS = -32602;
    private static final int INTERNAL_ERROR = -32603;
    private static final int RESOURCE_NOT_FOUND = -32002;

    private static final String ENTITIES_URI = "schema://entities";

    private final SchemaService schemaService;
    private final DbService dbService;
    private final Map<String, Tool> tools = new LinkedHashMap<>();

    /** A tool with at most one (string, required) argument. */
    private static final class Tool
    {
        final String name;
        final String description;
        final String argument;
        final String argumentDescription;
        final Function<String, Object> handler;

        Tool(String name, String description, String argument, String argumentDescription,
             Function<String, Object> handler)
        {
            this.name = name;
            this.description = description;
            this.argument = argument;
            this.argumentDescription = argumentDescription;
            this.handler = handler;
        }
    }

    @Inject
    public McpService(SchemaService schemaService, DbService dbService)
    {
        this.schemaService = schemaService;
        this.dbService = dbService;

        addTool("list_entities", "List all entities (tables) in the database",
                null, null, arg -> schemaService.getEntities());
        addTool("get_entity_schema", "Get detailed schema for a specific entity, including column references",
                "entityName", "Entity name", schemaService::getEntitySchemaWithReferences);
        addTool("get_entity_references", "Get all references (foreign keys) for a specific entity",
                "entityName", "Entity name", schemaService::getEntityReferences);
        addTool("list_db_columns", "List all columns from database metadata",
                null, null, arg -> schemaService.getDatabaseColumns());
        addTool("get_table_info", "Get detailed information about a specific table",
                "tableName", "Table name", schemaService::getTableInfo);
        addTool("execute_sql", "Execute a SELECT SQL query and return results",
                "sql", "SQL query to execute", this::executeQuery);
    }

    private void addTool(String name, String description, String argument, String argumentDescription,
                         Function<String, Object> handler)
    {
        tools.put(name, new Tool(name, description, argument, argumentDescription, handler));
    }

    /**
     * Handles one JSON-RPC message; null when nothing is to be answered (a notification or a response).
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> handleRequest(Map<String, Object> request)
    {
        Object id = request.get("id");
        Object method = request.get("method");
        if (id == null || method == null)
        {
            return null;
        }

        Map<String, Object> params = request.get("params") instanceof Map
                ? (Map<String, Object>) request.get("params") : Collections.<String, Object>emptyMap();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);

        try
        {
            switch (method.toString())
            {
                case "initialize":
                    response.put("result", handleInitialize(params));
                    break;
                case "ping":
                    response.put("result", new LinkedHashMap<>());
                    break;
                case "tools/list":
                    response.put("result", handleToolsList());
                    break;
                case "tools/call":
                    Tool tool = tools.get(String.valueOf(params.get("name")));
                    if (tool == null)
                    {
                        response.put("error", createError(INVALID_PARAMS, "Unknown tool: " + params.get("name")));
                        break;
                    }
                    response.put("result", handleToolCall(tool, params.get("arguments") instanceof Map
                            ? (Map<String, Object>) params.get("arguments") : Collections.<String, Object>emptyMap()));
                    break;
                case "resources/list":
                    response.put("result", handleResourcesList());
                    break;
                case "resources/templates/list":
                    response.put("result", Collections.singletonMap("resourceTemplates", new ArrayList<>()));
                    break;
                case "resources/read":
                    if (!ENTITIES_URI.equals(params.get("uri")))
                    {
                        response.put("error", createError(RESOURCE_NOT_FOUND, "Unknown resource: " + params.get("uri")));
                        break;
                    }
                    response.put("result", handleResourceRead());
                    break;
                default:
                    response.put("error", createError(METHOD_NOT_FOUND, "Method not found"));
            }
        }
        catch (Exception e)
        {
            log.log(Level.SEVERE, "Error in MCP method " + method, e);
            response.put("error", createError(INTERNAL_ERROR, String.valueOf(e.getMessage())));
        }

        return response;
    }

    /**
     * Answers with the protocol version the client asked for when it is supported, otherwise with the latest one.
     */
    private Map<String, Object> handleInitialize(Map<String, Object> params)
    {
        Object requested = params.get("protocolVersion");

        Map<String, Object> capabilities = new LinkedHashMap<>();
        capabilities.put("tools", Collections.singletonMap("listChanged", false));
        capabilities.put("resources", new LinkedHashMap<>());

        Map<String, Object> serverInfo = new LinkedHashMap<>();
        serverInfo.put("name", "be5-mcp-server");
        serverInfo.put("version", "1.1.0");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("protocolVersion", PROTOCOL_VERSIONS.contains(requested) ? requested : PROTOCOL_VERSIONS.get(0));
        result.put("capabilities", capabilities);
        result.put("serverInfo", serverInfo);
        return result;
    }

    private Map<String, Object> handleToolsList()
    {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Tool tool : tools.values())
        {
            Map<String, Object> schema = new LinkedHashMap<>();
            schema.put("type", "object");
            Map<String, Object> properties = new LinkedHashMap<>();
            if (tool.argument != null)
            {
                Map<String, Object> property = new LinkedHashMap<>();
                property.put("type", "string");
                property.put("description", tool.argumentDescription);
                properties.put(tool.argument, property);
                schema.put("required", Collections.singletonList(tool.argument));
            }
            schema.put("properties", properties);

            Map<String, Object> description = new LinkedHashMap<>();
            description.put("name", tool.name);
            description.put("description", tool.description);
            description.put("inputSchema", schema);
            // all tools only read
            description.put("annotations", Collections.singletonMap("readOnlyHint", true));
            list.add(description);
        }
        return Collections.<String, Object>singletonMap("tools", list);
    }

    /**
     * A failure of the tool itself is a result with isError, so that the model sees it and can correct the call.
     */
    private Map<String, Object> handleToolCall(Tool tool, Map<String, Object> arguments)
    {
        String text;
        boolean isError = false;
        try
        {
            Object argument = tool.argument == null ? null : arguments.get(tool.argument);
            if (tool.argument != null && argument == null)
            {
                throw new IllegalArgumentException("Missing argument: " + tool.argument);
            }
            text = JSONB.toJson(tool.handler.apply(argument == null ? null : argument.toString()));
        }
        catch (Exception e)
        {
            Throwable cause = e;
            while (cause.getCause() != null && cause.getMessage() == null) cause = cause.getCause();
            text = cause.getMessage() != null ? cause.getMessage() : cause.toString();
            isError = true;
        }

        Map<String, Object> textContent = new LinkedHashMap<>();
        textContent.put("type", "text");
        textContent.put("text", text);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", Collections.singletonList(textContent));
        result.put("isError", isError);
        return result;
    }

    private List<Map<String, Object>> executeQuery(String sql)
    {
        List<Map<String, Object>> results = new ArrayList<>();
        String trimmedSql = sql.trim().toLowerCase();
        boolean isSelect = trimmedSql.startsWith("select") || trimmedSql.startsWith("show")
                || trimmedSql.startsWith("describe") || trimmedSql.startsWith("explain");
        if (!isSelect)
        {
            throw new RuntimeException("Only SELECT queries are allowed");
        }

        dbService.execute(conn -> {
            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery(sql))
            {
                java.sql.ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();

                while (rs.next())
                {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++)
                    {
                        String columnName = meta.getColumnLabel(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    results.add(row);
                }
            }
            return null;
        });

        return results;
    }

    private Map<String, Object> handleResourcesList()
    {
        Map<String, Object> schemaResource = new LinkedHashMap<>();
        schemaResource.put("uri", ENTITIES_URI);
        schemaResource.put("name", "All Entities");
        schemaResource.put("description", "List of all entities in the database");
        schemaResource.put("mimeType", "application/json");

        return Collections.<String, Object>singletonMap("resources", Collections.singletonList(schemaResource));
    }

    private Map<String, Object> handleResourceRead()
    {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("uri", ENTITIES_URI);
        content.put("mimeType", "application/json");
        content.put("text", JSONB.toJson(schemaService.getEntities()));

        return Collections.<String, Object>singletonMap("contents", Collections.singletonList(content));
    }

    private Map<String, Object> createError(int code, String message)
    {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", code);
        error.put("message", message);
        return error;
    }
}