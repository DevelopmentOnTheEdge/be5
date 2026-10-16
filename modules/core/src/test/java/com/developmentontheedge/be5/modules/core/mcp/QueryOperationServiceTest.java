package com.developmentontheedge.be5.modules.core.mcp;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDBTest;
import com.developmentontheedge.be5.util.JsonUtils;
import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * The query and operation tools on the users entity of the core module, called as an MCP client calls them.
 */
public class QueryOperationServiceTest extends CoreBe5ProjectDBTest
{
    private static final String USER = "mcpToolsTestUser";

    @Inject
    private McpService mcpService;

    @Before
    public void init()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);
        db.update("DELETE FROM users WHERE user_name = ?", USER);
    }

    @After
    public void tearDown()
    {
        db.update("DELETE FROM users WHERE user_name = ?", USER);
        initGuest();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void listQueries()
    {
        List<Map<String, Object>> entities = (List<Map<String, Object>>) call("list_queries",
                args("entityName", "users"));

        assertEquals(1, entities.size());
        assertEquals("users", entities.get(0).get("entityName"));
        List<Map<String, Object>> queries = (List<Map<String, Object>>) entities.get(0).get("queries");
        Map<String, Object> allRecords = find(queries, "queryName", "All records");
        assertEquals("1D", allRecords.get("type"));
        assertNull("selection views are left out", find(queries, "queryName", "*** Selection view ***"));
        assertNull("only for Guest", find(queries, "queryName", "Login"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void describeQuery()
    {
        Map<String, Object> query = (Map<String, Object>) call("describe_query",
                args("entityName", "users", "queryName", "All records"));

        assertEquals("user_name", query.get("primaryKey"));
        assertEquals("TopFilter", query.get("filterOperation"));
        assertEquals("users", query.get("filterEntity"));
        List<Map<String, Object>> operations = (List<Map<String, Object>>) query.get("operations");
        assertEquals("atLeastOne", find(operations, "operationName", "Delete").get("selectedRows"));
        assertEquals("exactlyOne", find(operations, "operationName", "Edit").get("selectedRows"));
        assertEquals("optional", find(operations, "operationName", "Insert").get("selectedRows"));
        assertNull("only for Guest", find(operations, "operationName", "Login"));

        Map<String, Object> selectionView = (Map<String, Object>) call("describe_query",
                args("entityName", "users", "queryName", "*** Selection view ***"));
        assertEquals(Collections.singletonList(Collections.singletonMap("name", "selector")),
                selectionView.get("parameters"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void insertFindEditDelete()
    {
        java.sql.Timestamp start = new java.sql.Timestamp(System.currentTimeMillis() - 1000);
        Map<String, Object> form = (Map<String, Object>) call("get_operation_parameters",
                args("entityName", "users", "queryName", "All records", "operationName", "Insert"));
        Map<String, Object> userName = find((List<Map<String, Object>>) form.get("fields"), "name", "user_name");
        assertEquals(true, userName.get("required"));
        assertEquals("string", userName.get("type"));
        assertEquals(0L, count());

        Map<String, Object> inserted = (Map<String, Object>) call("execute_operation",
                args("entityName", "users", "queryName", "All records", "operationName", "Insert",
                        "values", args("user_name", USER, "user_pass", "secret")));
        assertEquals(true, inserted.get("executed"));
        assertEquals(1L, count());

        Map<String, Object> page = (Map<String, Object>) call("execute_query",
                args("entityName", "users", "queryName", "All records", "params", args("user_name", USER)));
        assertEquals(1, ((Number) page.get("totalNumberOfRows")).intValue());
        List<Map<String, Object>> rows = (List<Map<String, Object>>) page.get("rows");
        assertEquals(USER, rows.get(0).get("id"));
        assertEquals(Collections.singletonMap("User", USER), rows.get(0).get("values"));
        assertEquals(Collections.singletonList(args("name", "User", "title", "User")), page.get("columns"));

        Map<String, Object> editForm = (Map<String, Object>) call("get_operation_parameters",
                args("entityName", "users", "queryName", "All records", "operationName", "Edit",
                        "selectedRows", Collections.singletonList(USER)));
        assertEquals("secret", find((List<Map<String, Object>>) editForm.get("fields"), "name", "user_pass")
                .get("value"));

        Map<String, Object> deleted = (Map<String, Object>) call("execute_operation",
                args("entityName", "users", "queryName", "All records", "operationName", "Delete",
                        "selectedRows", Collections.singletonList(USER)));
        assertEquals(true, deleted.get("executed"));
        assertEquals(0L, count());

        // the actions of the agent are in the event log as the actions of the user in the UI
        assertEquals(Long.valueOf(1), db.oneLong("SELECT COUNT(*) FROM be5events WHERE action = 'operation' "
                + "AND entity = 'users' AND title = 'Delete' AND startTime >= ?", start));
        assertTrue(db.oneLong("SELECT COUNT(*) FROM be5events WHERE action = 'query' "
                + "AND entity = 'users' AND title = 'All records' AND startTime >= ?", start) >= 1);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void nullParametersAreLeftOut()
    {
        Map<String, Object> page = (Map<String, Object>) call("execute_query",
                args("entityName", "users", "queryName", "All records", "params", args("user_name", null),
                        "limit", 1));
        assertEquals(1, ((List<?>) page.get("rows")).size());
        assertTrue(((Number) page.get("totalNumberOfRows")).intValue() >= 1);
    }

    @Test
    public void selectedRowsAreChecked()
    {
        String error = callError("execute_operation", args("entityName", "users", "queryName", "All records",
                "operationName", "Delete"));
        assertTrue(error, error.contains("at least one selected row"));

        error = callError("get_operation_parameters", args("entityName", "users", "queryName", "All records",
                "operationName", "Edit", "selectedRows", Arrays.asList("a", "b")));
        assertTrue(error, error.contains("exactly one selected row"));
    }

    @Test
    public void accessIsChecked()
    {
        initGuest();
        String error = callError("execute_query", args("entityName", "users", "queryName", "All records"));
        assertFalse(error, error.isEmpty());
        error = callError("execute_operation", args("entityName", "users", "queryName", "All records",
                "operationName", "Insert", "values", args("user_name", USER)));
        assertFalse(error, error.isEmpty());
        assertEquals(0L, count());
    }

    @Test
    public void describeField()
    {
        DynamicProperty property = new DynamicProperty("status", "Status", String.class, "open");
        property.setAttribute(BeanInfoConstants.TAG_LIST_ATTR,
                new String[][]{{"open", "Open"}, {"closed", "Closed"}});
        property.setAttribute(BeanInfoConstants.RELOAD_ON_CHANGE, true);
        property.setAttribute(BeanInfoConstants.STATUS, "error");
        property.setAttribute(BeanInfoConstants.MESSAGE, "Choose another status");

        Map<String, Object> field = QueryOperationService.describeField(property);

        assertEquals("status", field.get("name"));
        assertEquals("Status", field.get("title"));
        assertEquals("open", field.get("value"));
        assertEquals(true, field.get("required"));
        assertEquals(true, field.get("reloadOnChange"));
        assertEquals(Arrays.asList(args("value", "open", "title", "Open"), args("value", "closed", "title", "Closed")),
                field.get("options"));
        assertEquals("error", field.get("status"));
        assertEquals("Choose another status", field.get("message"));
        assertNull(field.get("description"));

        DynamicProperty date = new DynamicProperty("from", "From", java.sql.Date.class,
                java.sql.Date.valueOf("2026-10-16"));
        date.setCanBeNull(true);
        Map<String, Object> dateField = QueryOperationService.describeField(date);
        assertEquals("date", dateField.get("type"));
        assertEquals("2026-10-16", dateField.get("value"));
        assertNull(dateField.get("required"));
    }

    @Test
    public void toRequestValues()
    {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("count", new java.math.BigDecimal("5"));
        values.put("flag", true);
        values.put("ids", Arrays.asList(new java.math.BigDecimal("1"), "2"));
        values.put("none", null);

        Map<String, Object> request = QueryOperationService.toRequestValues(values);

        assertEquals("5", request.get("count"));
        assertEquals("true", request.get("flag"));
        assertTrue(Arrays.equals(new String[]{"1", "2"}, (String[]) request.get("ids")));
        assertTrue(request.containsKey("none"));
        assertNull(request.get("none"));
        assertFalse(QueryOperationService.toContextParams(values).containsKey("none"));
    }

    private long count()
    {
        return db.oneLong("SELECT COUNT(*) FROM users WHERE user_name = ?", USER);
    }

    @SuppressWarnings("unchecked")
    private Object call(String tool, Map<String, Object> arguments)
    {
        Map<String, Object> result = callTool(tool, arguments);
        String text = (String) ((List<Map<String, Object>>) result.get("content")).get(0).get("text");
        assertEquals(text, false, result.get("isError"));
        return JsonUtils.getMapFromJson("{\"r\":" + text + "}").get("r");
    }

    @SuppressWarnings("unchecked")
    private String callError(String tool, Map<String, Object> arguments)
    {
        Map<String, Object> result = callTool(tool, arguments);
        String text = (String) ((List<Map<String, Object>>) result.get("content")).get(0).get("text");
        assertEquals(text, true, result.get("isError"));
        return text;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callTool(String tool, Map<String, Object> arguments)
    {
        Map<String, Object> params = new HashMap<>();
        params.put("name", tool);
        params.put("arguments", arguments);
        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("id", "1");
        request.put("method", "tools/call");
        request.put("params", params);
        return (Map<String, Object>) mcpService.handleRequest(request).get("result");
    }

    private static Map<String, Object> find(List<Map<String, Object>> list, String key, Object value)
    {
        return list.stream().filter(item -> value.equals(item.get(key))).findFirst().orElse(null);
    }

    private static Map<String, Object> args(Object... keysAndValues)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2)
        {
            map.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
    }
}
