package com.developmentontheedge.be5.modules.core.mcp;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.server.services.QueryBuilderAccess;
import com.developmentontheedge.be5.util.JsonUtils;

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

    /** How the tools for queries and operations are used together. */
    static final String APPLICATION_INSTRUCTIONS = "This is a BeanExplorer (be5) application: its UI is generated "
            + "from metadata. "
            + "Queries show tables of records, operations perform business actions, usually on records selected "
            + "in a query. Work as a user of the UI does: "
            + "1) list_queries finds the queries (the descriptions tell what they show), describe_query shows "
            + "the parameters of a query and the operations that can be run from it; "
            + "2) execute_query reads its rows, the id of a row is what an operation gets in selectedRows; "
            + "count_query_records counts the rows with the same parameters and filters, without reading them; "
            + "3) get_operation_parameters shows the form the user fills in for an operation, without running it. "
            + "A field with reloadOnChange changes the form: call it again with the values chosen so far; "
            + "4) execute_operation submits the values. When they are not accepted the form comes back with "
            + "messages on the fields: correct them and submit again. "
            + "A field of type file takes {\"fileId\": ...} of a file uploaded by the link from upload_file, "
            + "or for a small file its content: {\"name\": ..., \"mimeType\": ..., \"base64\": ...}. "
            + "Operations change data: unless the user asked for this very action, confirm with the user first, "
            + "always when requiresConfirmation is set.";

    /** Told on initialize to a client of a user who may use the Query Builder, and so execute_sql. */
    static final String INSTRUCTIONS = APPLICATION_INSTRUCTIONS
            + " The schema tools and execute_sql show the database below the application.";

    /** Told on initialize to a client of any other user. */
    static final String SCHEMA_INSTRUCTIONS = APPLICATION_INSTRUCTIONS
            + " The schema tools show the structure of the database below the application.";

    private final SchemaService schemaService;
    private final DbService dbService;
    private final QueryBuilderAccess queryBuilderAccess;
    private final Map<String, Tool> tools = new LinkedHashMap<>();

    private static final class Tool
    {
        final String name;
        final String description;
        final Arguments arguments;
        final boolean readOnly;
        final boolean destructive;
        /**
         * A tool that reads any data of the database (execute_sql), which only a user with access to the Query
         * Builder may use: the others get only the data that queries and operations show.
         */
        final boolean queryBuilderOnly;
        final Function<Map<String, Object>, Object> handler;

        Tool(String name, String description, Arguments arguments, boolean readOnly, boolean destructive,
             boolean queryBuilderOnly, Function<Map<String, Object>, Object> handler)
        {
            this.name = name;
            this.description = description;
            this.arguments = arguments;
            this.readOnly = readOnly;
            this.destructive = destructive;
            this.queryBuilderOnly = queryBuilderOnly;
            this.handler = handler;
        }
    }

    /** The arguments of a tool, as its input JSON schema shows them. */
    private static final class Arguments
    {
        final Map<String, Object> properties = new LinkedHashMap<>();
        final List<String> required = new ArrayList<>();

        Arguments add(String name, String type, String description, boolean isRequired)
        {
            Map<String, Object> property = new LinkedHashMap<>();
            property.put("type", type);
            property.put("description", description);
            if ("array".equals(type))
            {
                property.put("items", Collections.singletonMap("type", "string"));
            }
            properties.put(name, property);
            if (isRequired) required.add(name);
            return this;
        }

        Map<String, Object> schema()
        {
            Map<String, Object> schema = new LinkedHashMap<>();
            schema.put("type", "object");
            if (!required.isEmpty()) schema.put("required", required);
            schema.put("properties", properties);
            return schema;
        }
    }

    @Inject
    public McpService(SchemaService schemaService, DbService dbService, QueryOperationService queryOperationService,
                      QueryBuilderAccess queryBuilderAccess)
    {
        this.schemaService = schemaService;
        this.dbService = dbService;
        this.queryBuilderAccess = queryBuilderAccess;

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
        // any data of the database: only for the users of the Query Builder
        addTool("execute_sql", "Execute a SELECT SQL query and return results",
                "sql", "SQL query to execute", true, this::executeQuery);

        addTool("list_queries", "List the queries (views of records, as in the menu of the UI) the user may open, "
                        + "grouped by entity, with a description of what each one shows",
                new Arguments().add("entityName", "string", "Only the queries of this entity", false), true, false,
                args -> queryOperationService.listQueries(string(args, "entityName")));
        addTool("describe_query", "Describe a query: its parameters, the entity whose columns filter it, "
                        + "and the operations that can be run from it - with their descriptions, how many selected "
                        + "rows each needs and whether it asks for confirmation",
                new Arguments()
                        .add("entityName", "string", "Entity name", true)
                        .add("queryName", "string", "Query name", true), true, false,
                args -> queryOperationService.describeQuery(string(args, "entityName"), string(args, "queryName")));
        addTool("execute_query", "Read one page of the rows of a query, as the UI shows them. Every row has an id "
                        + "that an operation takes in selectedRows",
                queryArguments()
                        .add("offset", "integer", "Index of the first row, 0 by default", false)
                        .add("limit", "integer", "Number of rows, " + QueryOperationService.DEFAULT_LIMIT
                                + " by default, at most " + QueryOperationService.MAX_LIMIT, false), true, false,
                args -> queryOperationService.executeQuery(string(args, "entityName"), string(args, "queryName"),
                        object(args, "params"), integer(args, "offset", 0),
                        integer(args, "limit", QueryOperationService.DEFAULT_LIMIT)));
        addTool("count_query_records", "Count the rows of a query with the parameters and filters, without reading "
                        + "them: answers \"how many records ...\" with a SELECT COUNT(*) over the query, so only "
                        + "the records the query shows the user are counted",
                queryArguments(), true, false,
                args -> queryOperationService.countRecords(string(args, "entityName"), string(args, "queryName"),
                        object(args, "params")));
        addTool("get_operation_parameters", "Show the form of an operation: the fields the user fills in, with "
                        + "their types, current values, options and messages. Nothing is executed. Call it again "
                        + "with values after changing a field marked reloadOnChange",
                operationArguments(), true, false,
                args -> queryOperationService.getOperationParameters(string(args, "entityName"),
                        string(args, "queryName"), string(args, "operationName"), object(args, "params"),
                        strings(args, "selectedRows"), object(args, "values")));
        addTool("execute_operation", "Execute an operation with the values of its form, as the user submitting it "
                        + "in the UI. This changes data. When the values are not accepted the form comes back "
                        + "(executed: false) with messages on the fields",
                operationArguments(), false, true,
                args -> queryOperationService.executeOperation(string(args, "entityName"),
                        string(args, "queryName"), string(args, "operationName"), object(args, "params"),
                        strings(args, "selectedRows"), object(args, "values")));
        addTool("upload_file", "Get a one-time link to upload a file for a file field of an operation: the user "
                        + "opens it in a browser and chooses the file, or the agent sends it with "
                        + "curl -F file=@<path> '<link>'. Then the file is the value {\"fileId\": ...} of the field. "
                        + "Small files can instead be given inline as {\"name\", \"mimeType\", \"base64\"}",
                new Arguments().add("purpose", "string", "What the file is for, shown to the user on the upload "
                        + "page, e.g. \"Signed contract for order 1042 (PDF)\"", false), false, false,
                args -> queryOperationService.createUpload(string(args, "purpose")));
    }

    private static Arguments queryArguments()
    {
        return new Arguments()
                .add("entityName", "string", "Entity name", true)
                .add("queryName", "string", "Query name", true)
                .add("params", "object", "Query parameters by name (see describe_query) and filters by a column "
                        + "of the filter entity, e.g. {\"status\": \"open\"}; a list for several values", false);
    }

    private static Arguments operationArguments()
    {
        return queryArguments()
                .add("operationName", "string", "Operation name, one of the operations of the query", true)
                .add("selectedRows", "array", "Ids of the rows the operation works on, from execute_query", false)
                .add("values", "object", "Values of the form fields by field name; the fields left out keep the "
                        + "values the form gives them. A file field takes {\"fileId\": ...} from upload_file "
                        + "or {\"name\": ..., \"mimeType\": ..., \"base64\": ...}, a list for several files", false);
    }

    private void addTool(String name, String description, String argument, String argumentDescription,
                         Function<String, Object> handler)
    {
        addTool(name, description, argument, argumentDescription, false, handler);
    }

    /**
     * A read-only tool with at most one (string, required) argument.
     */
    private void addTool(String name, String description, String argument, String argumentDescription,
                         boolean queryBuilderOnly, Function<String, Object> handler)
    {
        Arguments arguments = new Arguments();
        if (argument != null) arguments.add(argument, "string", argumentDescription, true);
        tools.put(name, new Tool(name, description, arguments, true, false, queryBuilderOnly,
                args -> handler.apply(string(args, argument))));
    }

    private void addTool(String name, String description, Arguments arguments, boolean readOnly,
                         boolean destructive, Function<Map<String, Object>, Object> handler)
    {
        tools.put(name, new Tool(name, description, arguments, readOnly, destructive, false, handler));
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
                    response.put("result", Collections.singletonMap("tools", describeTools()));
                    break;
                case "tools/call":
                    Map<String, Object> result = callTool(String.valueOf(params.get("name")),
                            params.get("arguments") instanceof Map ? (Map<String, Object>) params.get("arguments")
                                    : Collections.<String, Object>emptyMap());
                    if (result == null)
                    {
                        response.put("error", createError(INVALID_PARAMS, "Unknown tool: " + params.get("name")));
                        break;
                    }
                    response.put("result", result);
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
        serverInfo.put("version", "1.2.0");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("protocolVersion", PROTOCOL_VERSIONS.contains(requested) ? requested : PROTOCOL_VERSIONS.get(0));
        result.put("capabilities", capabilities);
        result.put("serverInfo", serverInfo);
        result.put("instructions", queryBuilderAccess() ? INSTRUCTIONS : SCHEMA_INSTRUCTIONS);
        return result;
    }

    /**
     * The definitions of the tools the logged in user may use, as tools/list gives them.
     */
    List<Map<String, Object>> describeTools()
    {
        boolean queryBuilderAccess = queryBuilderAccess();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Tool tool : tools.values())
        {
            if (tool.queryBuilderOnly && !queryBuilderAccess) continue;
            Map<String, Object> description = new LinkedHashMap<>();
            description.put("name", tool.name);
            description.put("description", tool.description);
            description.put("inputSchema", tool.arguments.schema());
            Map<String, Object> annotations = new LinkedHashMap<>();
            annotations.put("readOnlyHint", tool.readOnly);
            if (!tool.readOnly)
            {
                annotations.put("destructiveHint", tool.destructive);
                annotations.put("openWorldHint", false);
            }
            description.put("annotations", annotations);
            list.add(description);
        }
        return list;
    }

    /**
     * Calls a tool as the logged in user, as tools/call does; null when there is no such tool.
     */
    Map<String, Object> callTool(String name, Map<String, Object> arguments)
    {
        Tool tool = tools.get(name);
        if (tool == null) return null;
        if (tool.queryBuilderOnly && !queryBuilderAccess())
        {
            return toolResult("Tool " + name + " is for the users who may use the Query Builder (a role of "
                    + QueryBuilderAccess.ENTITY_NAME + ": " + QueryBuilderAccess.QUERY_NAME
                    + "): queries and operations show the data to the others", true);
        }
        return handleToolCall(tool, arguments);
    }

    /**
     * Whether the logged in user may read any data of the database with execute_sql: the user may use the
     * Query Builder, and its query is accessible to AI agents.
     */
    boolean queryBuilderAccess()
    {
        return queryBuilderAccess.isAllowedForAi();
    }

    /**
     * Whether the tool changes data, like a submit of an operation.
     */
    boolean isDestructive(String name)
    {
        Tool tool = tools.get(name);
        return tool != null && tool.destructive;
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
            for (String required : tool.arguments.required)
            {
                if (arguments.get(required) == null)
                {
                    throw new IllegalArgumentException("Missing argument: " + required);
                }
            }
            text = JSONB.toJson(tool.handler.apply(arguments));
        }
        catch (Exception e)
        {
            Throwable cause = e;
            while (cause.getCause() != null && cause.getMessage() == null) cause = cause.getCause();
            text = cause.getMessage() != null ? cause.getMessage() : cause.toString();
            isError = true;
        }

        return toolResult(text, isError);
    }

    private static Map<String, Object> toolResult(String text, boolean isError)
    {
        Map<String, Object> textContent = new LinkedHashMap<>();
        textContent.put("type", "text");
        textContent.put("text", text);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", Collections.singletonList(textContent));
        result.put("isError", isError);
        return result;
    }

    private static String string(Map<String, Object> arguments, String name)
    {
        Object value = name == null ? null : arguments.get(name);
        return value == null ? null : value.toString();
    }

    private static int integer(Map<String, Object> arguments, String name, int defaultValue)
    {
        Object value = arguments.get(name);
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).intValue();
        try
        {
            return Integer.parseInt(value.toString().trim());
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Argument " + name + " must be an integer: " + value);
        }
    }

    /**
     * An object argument; some clients send it as a JSON string.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(Map<String, Object> arguments, String name)
    {
        Object value = arguments.get(name);
        if (value == null) return Collections.emptyMap();
        if (value instanceof Map) return (Map<String, Object>) value;
        if (value instanceof String && ((String) value).trim().startsWith("{"))
        {
            return JsonUtils.getMapFromJson((String) value);
        }
        if (value instanceof String && ((String) value).trim().isEmpty()) return Collections.emptyMap();
        throw new IllegalArgumentException("Argument " + name + " must be an object: " + value);
    }

    /**
     * A list of strings; a single value stands for a list of it.
     */
    private static List<String> strings(Map<String, Object> arguments, String name)
    {
        Object value = arguments.get(name);
        if (value == null) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        if (value instanceof List)
        {
            for (Object item : (List<?>) value)
            {
                if (item != null) result.add(item instanceof java.math.BigDecimal
                        ? ((java.math.BigDecimal) item).toPlainString() : item.toString());
            }
        }
        else if (!value.toString().trim().isEmpty())
        {
            result.add(value instanceof java.math.BigDecimal
                    ? ((java.math.BigDecimal) value).toPlainString() : value.toString());
        }
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