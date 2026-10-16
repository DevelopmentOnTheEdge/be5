package com.developmentontheedge.be5.modules.core.mcp;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDBTest;
import com.developmentontheedge.be5.security.UserInfo;
import com.developmentontheedge.be5.security.UserInfoHolder;
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

    @After
    public void tearDown()
    {
        initGuest();
    }

    @Test
    public void onlyTheToolsOfTheApplication()
    {
        List<String> names = tools.list().stream().map(tool -> (String) tool.get("name")).collect(Collectors.toList());
        assertEquals(Arrays.asList("list_queries", "describe_query", "execute_query", "get_operation_parameters",
                "execute_operation", "upload_file"), names);
        assertFalse(ApplicationTools.INSTRUCTIONS.contains("execute_sql"));
        assertTrue(tools.changesData("execute_operation"));
        assertFalse(tools.changesData("execute_query"));
        assertFalse(tools.changesData("upload_file"));

        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);
        ApplicationTools.Result sql = tools.call("execute_sql", args("sql", "SELECT user_pass FROM users"));
        assertTrue(sql.isError());
        assertEquals("Unknown tool: execute_sql", sql.getText());
        assertTrue(tools.call("list_entities", args()).isError());
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
