package com.developmentontheedge.be5.modules.core.mcp;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDBTest;
import com.developmentontheedge.be5.security.UserInfo;
import com.developmentontheedge.be5.security.UserInfoHolder;
import com.developmentontheedge.be5.server.services.QueryBuilderAccess;
import org.junit.After;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * The tools for a chatbot inside the application: queries and operations only, with the rights of the user.
 */
public class ApplicationToolsTest extends CoreBe5ProjectDBTest
{
    @Inject
    private ApplicationTools tools;
    @Inject
    private QueryBuilderAccess queryBuilderAccess;

    @After
    public void tearDown()
    {
        initGuest();
    }

    @Test
    public void sqlOnlyWithQueryBuilderAccess()
    {
        assertFalse(ApplicationTools.INSTRUCTIONS.contains("execute_sql"));
        assertTrue(tools.changesData("execute_operation"));
        assertFalse(tools.changesData("execute_query"));
        assertFalse(tools.changesData("upload_file"));
        assertFalse(tools.changesData("execute_sql"));

        // an administrator may not use the Query Builder, so neither execute_sql
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR);
        assertEquals(Arrays.asList("list_entities", "get_entity_schema", "get_entity_references", "list_db_columns",
                "get_table_info", "list_queries", "describe_query", "execute_query", "count_query_records",
                "get_operation_parameters", "execute_operation", "upload_file"), names(tools.list()));
        ApplicationTools.Result sql = tools.call("execute_sql", args("sql", "SELECT user_pass FROM users"));
        assertTrue(sql.isError());
        assertTrue(sql.getText(), sql.getText().contains("Query Builder"));
        ApplicationTools.Result schema = tools.call("get_entity_schema", args("entityName", "users"));
        assertFalse(schema.getText(), schema.isError());
        assertTrue(schema.getText(), schema.getText().contains("user_pass"));
        assertTrue(tools.call("no_such_tool", args()).getText().startsWith("Unknown tool"));

        initUserWithRoles(RoleType.ROLE_SYSTEM_DEVELOPER);
        assertTrue(names(tools.list()).contains("execute_sql"));
        ApplicationTools.Result count = tools.call("execute_sql", args("sql", "SELECT COUNT(*) AS \"n\" FROM users"));
        assertFalse(count.getText(), count.isError());
        assertTrue(count.getText(), count.getText().startsWith("[{\"n\":"));
    }

    @Test
    public void aRoleGivenTheQueryBuilderGetsSql()
    {
        // the test project gives the role User the menu item of the Query Builder (src/meta/modules/core/_system_.yaml)
        assertTrue(meta.getQuery("_system_", "Query Builder").getRoles().getFinalRoles().contains("User"));
        assertTrue("the rest of _system_ stays", meta.getQuery("_system_", "System") != null);

        initUserWithRoles("User");
        assertTrue(queryBuilderAccess.isAllowed());
        assertTrue(names(tools.list()).contains("execute_sql"));
        ApplicationTools.Result count = tools.call("execute_sql", args("sql", "SELECT COUNT(*) AS \"n\" FROM users"));
        assertFalse(count.getText(), count.isError());

        initUserWithRoles("TestUser2");
        assertFalse(queryBuilderAccess.isAllowed());
        assertFalse(names(tools.list()).contains("execute_sql"));
    }

    @Test
    public void noSqlWhenTheQueryBuilderIsNotAiAccessible()
    {
        initUserWithRoles(RoleType.ROLE_SYSTEM_DEVELOPER);
        Query queryBuilder = meta.getQuery(QueryBuilderAccess.ENTITY_NAME, QueryBuilderAccess.QUERY_NAME);
        queryBuilder.setAiAccessible(false);
        try
        {
            assertTrue("the Query Builder itself stays", queryBuilderAccess.isAllowed());
            assertFalse(names(tools.list()).contains("execute_sql"));
            assertTrue(tools.call("execute_sql", args("sql", "SELECT COUNT(*) FROM users")).isError());
        }
        finally
        {
            queryBuilder.setAiAccessible(true);
        }
        assertTrue(names(tools.list()).contains("execute_sql"));
    }

    @Test
    public void listForAGivenUser() throws Exception
    {
        initUserWithRoles(RoleType.ROLE_SYSTEM_DEVELOPER);
        UserInfo developer = UserInfoHolder.getLoggedUser();
        initGuest();
        AtomicReference<List<String>> names = new AtomicReference<>();

        Thread thread = new Thread(() -> names.set(names(tools.list(developer))));
        thread.start();
        thread.join();

        assertTrue(names.get().toString(), names.get().contains("execute_sql"));
        assertFalse(names(tools.list()).contains("execute_sql"));
    }

    private static List<String> names(List<Map<String, Object>> definitions)
    {
        return definitions.stream().map(tool -> (String) tool.get("name")).collect(Collectors.toList());
    }

    @Test
    public void queriesAndOperationsOfTheUser()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR);
        String adminQueries = tools.call("list_queries", args("entityName", "users")).getText();
        assertTrue(adminQueries, adminQueries.contains("\"queryName\":\"All records\""));
        assertFalse(adminQueries, adminQueries.contains("\"queryName\":\"Login\""));

        initGuest();
        ApplicationTools.Result guestQueries = tools.call("list_queries", args("entityName", "users"));
        assertFalse(guestQueries.getText(), guestQueries.isError());
        assertTrue(guestQueries.getText(), guestQueries.getText().contains("\"queryName\":\"Login\""));
        assertFalse(guestQueries.getText(), guestQueries.getText().contains("\"queryName\":\"All records\""));

        assertTrue(tools.call("describe_query", args("entityName", "users", "queryName", "All records")).isError());
        assertTrue(tools.call("execute_query", args("entityName", "users", "queryName", "All records")).isError());
    }

    @Test
    public void anOperationIsRunFromAQueryTheUserMayOpen()
    {
        // Login is for guests, but it is one of the operations of All records, which is for administrators only
        initGuest();
        ApplicationTools.Result result = tools.call("get_operation_parameters",
                args("entityName", "users", "queryName", "All records", "operationName", "Login"));

        assertTrue(result.getText(), result.isError());
        assertTrue(result.getText(), result.getText().contains("users.All records"));
    }

    @Test
    public void argumentsAsAnLlmGivesThem()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR);

        ApplicationTools.Result result = tools.call("execute_query",
                "{\"entityName\": \"users\", \"queryName\": \"All records\", \"limit\": 1}");
        assertFalse(result.getText(), result.isError());
        assertTrue(result.getText(), result.getText().contains("\"limit\":1"));

        assertTrue(tools.call("execute_query", "[1, 2]").isError());
        assertTrue(tools.call("list_queries", "").getText().startsWith("["));
    }

    @Test
    public void asAGivenUserInAThreadOfItsOwn() throws Exception
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR);
        UserInfo admin = UserInfoHolder.getLoggedUser();
        AtomicReference<ApplicationTools.Result> result = new AtomicReference<>();
        AtomicReference<UserInfo> userAfter = new AtomicReference<>();
        AtomicReference<Throwable> noUser = new AtomicReference<>();

        Thread thread = new Thread(() -> {
            try
            {
                tools.call("list_queries", args("entityName", "users"));
            }
            catch (IllegalStateException e)
            {
                noUser.set(e);
            }
            result.set(tools.call(admin, "list_queries", args("entityName", "users")));
            userAfter.set(UserInfoHolder.getLoggedUser());
        });
        thread.start();
        thread.join();

        assertTrue("no user in the thread", noUser.get() != null);
        assertFalse(result.get().getText(), result.get().isError());
        assertTrue(result.get().getText(), result.get().getText().contains("\"queryName\":\"All records\""));
        assertNull("the user of the thread is restored", userAfter.get());
    }

    @Test
    public void noUser()
    {
        UserInfo user = UserInfoHolder.getLoggedUser();
        UserInfoHolder.setLoggedUser(null);
        try
        {
            tools.call("list_queries", Collections.emptyMap());
            fail("a tool runs as a user");
        }
        catch (IllegalStateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().startsWith("No logged in user"));
        }
        finally
        {
            UserInfoHolder.setLoggedUser(user);
        }
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
