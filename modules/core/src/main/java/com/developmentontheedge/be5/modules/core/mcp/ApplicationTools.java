package com.developmentontheedge.be5.modules.core.mcp;

import com.developmentontheedge.be5.security.UserInfo;
import com.developmentontheedge.be5.security.UserInfoHolder;
import com.developmentontheedge.be5.util.JsonUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The MCP tools of queries and operations for an assistant built into the application: a chatbot that an external
 * LLM drives by asking for tool calls, which the chatbot runs here, in the application.
 * <p>
 * The tools work as the user the chatbot talks to, with the current roles of the user: they show only the queries
 * and operations the user may open and run, and run them as the user does in the UI (with the access checks and the
 * operation and event logs). The schema tools and execute_sql of the MCP endpoint are left out - they show the whole
 * database, whatever the rights of the user.
 * <p>
 * {@link #list()} gives the definitions as MCP does: name, description, inputSchema (a JSON schema, which is the
 * {@code input_schema} of a Claude tool or the {@code parameters} of an OpenAI function) and annotations. A call
 * gives the text to return to the LLM as the result of the tool call, and whether it is an error
 * ({@code is_error} of a Claude tool result).
 */
@Singleton
public class ApplicationTools
{
    /**
     * How the tools are used together, for the system prompt of the assistant.
     */
    public static final String INSTRUCTIONS = McpService.APPLICATION_INSTRUCTIONS;

    private final McpService mcpService;

    @Inject
    public ApplicationTools(McpService mcpService)
    {
        this.mcpService = mcpService;
    }

    /**
     * The definitions of the tools, the same for every user (what they show depends on the user).
     */
    public List<Map<String, Object>> list()
    {
        return mcpService.describeTools(true);
    }

    /**
     * Whether the tool changes data (execute_operation): the chatbot should ask the user before running it,
     * unless the user asked for this very action.
     */
    public boolean changesData(String name)
    {
        return mcpService.isDestructive(name);
    }

    /**
     * Runs a tool as the logged in user of the request the chatbot is serving.
     *
     * @throws IllegalStateException when there is no logged in user, e.g. in a thread of its own: then
     *                               {@link #call(UserInfo, String, Map)} with the user taken in the request
     */
    public Result call(String name, Map<String, Object> arguments)
    {
        if (UserInfoHolder.getLoggedUser() == null)
        {
            throw new IllegalStateException("No logged in user: call the tool in a request of the user, "
                    + "or pass the user taken there (UserInfoHolder.getLoggedUser())");
        }
        return run(name, arguments);
    }

    /**
     * Runs a tool with the arguments as an LLM gives them, a JSON object.
     */
    public Result call(String name, String argumentsJson)
    {
        Map<String, Object> arguments;
        try
        {
            arguments = argumentsJson == null || argumentsJson.trim().isEmpty()
                    ? Collections.<String, Object>emptyMap() : JsonUtils.getMapFromJson(argumentsJson);
        }
        catch (RuntimeException e)
        {
            return new Result("The arguments are not a JSON object: " + e.getMessage(), true);
        }
        return call(name, arguments);
    }

    /**
     * Runs a tool as the user - who is taken in a request of the user, for a chatbot running in a thread of its own.
     */
    public Result call(UserInfo user, String name, Map<String, Object> arguments)
    {
        if (user == null) throw new IllegalArgumentException("No user");
        UserInfo previousUser = UserInfoHolder.getLoggedUser();
        UserInfoHolder.setLoggedUser(user);
        try
        {
            return run(name, arguments);
        }
        finally
        {
            UserInfoHolder.setLoggedUser(previousUser);
        }
    }

    @SuppressWarnings("unchecked")
    private Result run(String name, Map<String, Object> arguments)
    {
        Map<String, Object> result = mcpService.callTool(name,
                arguments == null ? Collections.<String, Object>emptyMap() : arguments, true);
        if (result == null) return new Result("Unknown tool: " + name, true);
        List<Map<String, Object>> content = (List<Map<String, Object>>) result.get("content");
        return new Result((String) content.get(0).get("text"), Boolean.TRUE.equals(result.get("isError")));
    }

    /**
     * The result of a tool call: JSON, or the message of an error.
     */
    public static final class Result
    {
        private final String text;
        private final boolean error;

        Result(String text, boolean error)
        {
            this.text = text;
            this.error = error;
        }

        public String getText()
        {
            return text;
        }

        public boolean isError()
        {
            return error;
        }

        @Override
        public String toString()
        {
            return (error ? "error: " : "") + text;
        }
    }
}
