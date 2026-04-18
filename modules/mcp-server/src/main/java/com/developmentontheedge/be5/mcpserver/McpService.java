package com.developmentontheedge.be5.mcpserver;

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

@Singleton
public class McpService
{
    private static final Jsonb JSONB = JsonbBuilder.create();

    private final SchemaService schemaService;
    private final DbService dbService;

    @Inject
    public McpService(SchemaService schemaService, DbService dbService)
    {
        this.schemaService = schemaService;
        this.dbService = dbService;
    }

    public Map<String, Object> handleRequest(Map<String, Object> request)
    {
        String method = (String) request.get("method");
        Map<String, Object> params = (Map<String, Object>) request.get("params");
        String id = request.get("id") != null ? String.valueOf(request.get("id")) : null;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("jsonrpc", "2.0");
        if (id != null)
        {
            response.put("id", id);
        }

        if (id == null)
        {
            return null;
        }

        try
        {
            Object result = null;
            switch (method)
            {
                case "initialize":
                    result = handleInitialize();
                    break;
                case "tools/list":
                    result = handleToolsList();
                    break;
                case "tools/call":
                    result = handleToolCall(
                            (String) params.get("name"),
                            (Map<String, Object>) params.get("arguments"));
                    break;
                case "resources/list":
                    result = handleResourcesList();
                    break;
                case "resources/read":
                    result = handleResourceRead((String) params.get("uri"));
                    break;
                default:
                    response.put("error", createError(-32601, "Method not found"));
                    return response;
            }
            response.put("result", result);
        }
        catch (Exception e)
        {
            response.put("error", createError(-32603, e.getMessage()));
        }

        return response;
    }

    private Map<String, Object> handleInitialize()
    {
        Map<String, Object> capabilities = new LinkedHashMap<>();
        capabilities.put("tools", new LinkedHashMap<>());
        capabilities.put("resources", new LinkedHashMap<>());

        Map<String, Object> serverInfo = new LinkedHashMap<>();
        serverInfo.put("name", "be5-mcp-server");
        serverInfo.put("version", "1.0.0");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("protocolVersion", "2024-11-05");
        result.put("capabilities", capabilities);
        result.put("serverInfo", serverInfo);
        return result;
    }

    private Map<String, Object> handleToolsList()
    {
        List<Map<String, Object>> tools = new ArrayList<>();

        Map<String, Object> listEntitiesTool = new LinkedHashMap<>();
        listEntitiesTool.put("name", "list_entities");
        listEntitiesTool.put("description", "List all entities (tables) in the database");
        listEntitiesTool.put("inputSchema", createInputSchema(emptyMap(), emptyList()));
        tools.add(listEntitiesTool);

        Map<String, Object> getSchemaTool = new LinkedHashMap<>();
        getSchemaTool.put("name", "get_entity_schema");
        getSchemaTool.put("description", "Get detailed schema for a specific entity, including column references");
        Map<String, Object> schemaProps = new LinkedHashMap<>();
        schemaProps.put("entityName", createStringProperty("Entity name"));
        getSchemaTool.put("inputSchema", createInputSchema(schemaProps,
                Arrays.asList("entityName")));
        tools.add(getSchemaTool);

        Map<String, Object> getReferencesTool = new LinkedHashMap<>();
        getReferencesTool.put("name", "get_entity_references");
        getReferencesTool.put("description", "Get all references (foreign keys) for a specific entity");
        Map<String, Object> refProps = new LinkedHashMap<>();
        refProps.put("entityName", createStringProperty("Entity name"));
        getReferencesTool.put("inputSchema", createInputSchema(refProps,
                Arrays.asList("entityName")));
        tools.add(getReferencesTool);

        Map<String, Object> listDbColumnsTool = new LinkedHashMap<>();
        listDbColumnsTool.put("name", "list_db_columns");
        listDbColumnsTool.put("description", "List all columns from database metadata");
        listDbColumnsTool.put("inputSchema", createInputSchema(emptyMap(), emptyList()));
        tools.add(listDbColumnsTool);

        Map<String, Object> getTableInfoTool = new LinkedHashMap<>();
        getTableInfoTool.put("name", "get_table_info");
        getTableInfoTool.put("description", "Get detailed information about a specific table");
        Map<String, Object> tableProps = new LinkedHashMap<>();
        tableProps.put("tableName", createStringProperty("Table name"));
        getTableInfoTool.put("inputSchema", createInputSchema(tableProps,
                Arrays.asList("tableName")));
        tools.add(getTableInfoTool);

        Map<String, Object> executeSqlTool = new LinkedHashMap<>();
        executeSqlTool.put("name", "execute_sql");
        executeSqlTool.put("description", "Execute a SELECT SQL query and return results");
        Map<String, Object> sqlProps = new LinkedHashMap<>();
        sqlProps.put("sql", createStringProperty("SQL query to execute"));
        executeSqlTool.put("inputSchema", createInputSchema(sqlProps, Arrays.asList("sql")));
        tools.add(executeSqlTool);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tools", tools);
        return result;
    }

    private Map<String, Object> createInputSchema(Map<String, Object> properties, List<String> required)
    {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", required);
        return schema;
    }

    private Map<String, Object> createStringProperty(String description)
    {
        Map<String, Object> prop = new LinkedHashMap<>();
        prop.put("type", "string");
        prop.put("description", description);
        return prop;
    }

    private Map<String, Object> emptyMap()
    {
        return new LinkedHashMap<>();
    }

    private List<String> emptyList()
    {
        return new ArrayList<>();
    }

    private Map<String, Object> handleToolCall(String toolName, Map<String, Object> arguments)
    {
        Object data;
        switch (toolName)
        {
            case "list_entities":
                data = schemaService.getEntities();
                break;
            case "get_entity_schema":
                data = schemaService.getEntitySchemaWithReferences((String) arguments.get("entityName"));
                break;
            case "get_entity_references":
                data = schemaService.getEntityReferences((String) arguments.get("entityName"));
                break;
            case "list_db_columns":
                data = schemaService.getDatabaseColumns();
                break;
            case "get_table_info":
                data = schemaService.getTableInfo((String) arguments.get("tableName"));
                break;
            case "execute_sql":
                data = executeQuery((String) arguments.get("sql"));
                break;
            default:
                throw new RuntimeException("Unknown tool: " + toolName);
        }

        Map<String, Object> textContent = new LinkedHashMap<>();
        textContent.put("type", "text");
        textContent.put("text", JSONB.toJson(data));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", Collections.singletonList(textContent));
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
        List<Map<String, Object>> resources = new ArrayList<>();

        Map<String, Object> schemaResource = new LinkedHashMap<>();
        schemaResource.put("uri", "schema://entities");
        schemaResource.put("name", "All Entities");
        schemaResource.put("description", "List of all entities in the database");
        schemaResource.put("mimeType", "application/json");
        resources.add(schemaResource);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("resources", resources);
        return result;
    }

    private Map<String, Object> handleResourceRead(String uri)
    {
        Map<String, Object> content = new LinkedHashMap<>();
        if ("schema://entities".equals(uri))
        {
            content.put("text", JSONB.toJson(schemaService.getEntities()));
        }
        else
        {
            throw new RuntimeException("Unknown resource: " + uri);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("contents", Collections.singletonList(content));
        return result;
    }

    private Map<String, Object> createError(int code, String message)
    {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", code);
        error.put("message", message);
        return error;
    }
}