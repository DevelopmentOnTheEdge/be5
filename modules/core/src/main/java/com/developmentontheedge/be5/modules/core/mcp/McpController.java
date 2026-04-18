package com.developmentontheedge.be5.modules.core.mcp;

import com.developmentontheedge.be5.util.JsonUtils;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class McpController
{
    private final McpService mcpService;
    private final McpAuthenticationService authService;

    @Inject
    public McpController(McpService mcpService, McpAuthenticationService authService)
    {
        this.mcpService = mcpService;
        this.authService = authService;
    }

    private static final Jsonb JSONB = JsonbBuilder.create();

    public void handle(Request req, Response res) throws IOException
    {
        String method = extractMethod(req);
        if (McpMethod.requiresAuthentication(method))
        {
            String authHeader = req.getRawRequest().getHeader("Authorization");
            if (!authService.authenticate(authHeader))
            {
                res.getRawResponse().setHeader("WWW-Authenticate", "Basic realm=\"MCP Server\"");
                Map<String, Object> error = createErrorResponse(-32603, "Authentication required");
                res.sendAsJson(error, 401);
                return;
            }
        }

        String accept = req.getRawRequest().getHeader("Accept");
        boolean isSse = accept != null && accept.contains("text/event-stream");

        if (isSse)
        {
            handleSse(req, res);
        }
        else
        {
            handleJsonRpc(req, res);
        }
    }

    private String extractMethod(Request req)
    {
        try
        {
            String body = req.getBody();
            if (body != null && !body.isEmpty())
            {
                Map<String, Object> request = parseJson(body);
                return (String) request.get("method");
            }
        }
        catch (Exception ignored)
        {
        }
        return null;
    }

    private void handleJsonRpc(Request req, Response res) throws IOException
    {
        String body = req.getBody();
        if (body == null || body.isEmpty())
        {
            Map<String, Object> error = createErrorResponse(-32600, "Invalid Request");
            res.sendAsJson(error, 400);
            return;
        }

        try
        {
            Map<String, Object> request = parseJson(body);
            Map<String, Object> response = mcpService.handleRequest(request);
            if (response == null)
            {
                res.setStatus(202);
                return;
            }
            res.sendAsJson(response);
        }
        catch (Exception e)
        {
            Map<String, Object> error = createErrorResponse(-32603, e.getMessage());
            res.sendAsJson(error, 500);
        }
    }

    private void handleSse(Request req, Response res) throws IOException
    {
        HttpServletResponse raw = res.getRawResponse();
        raw.setContentType("text/event-stream");
        raw.setCharacterEncoding("UTF-8");
        raw.setStatus(200);

        String body = req.getBody();
        if (body == null || body.isEmpty())
        {
            raw.getWriter().write(
                    "data: {\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32600,\"message\":\"Invalid Request\"}}\n\n");
            raw.flushBuffer();
            return;
        }

        try
        {
            Map<String, Object> request = parseJson(body);
            Map<String, Object> response = mcpService.handleRequest(request);
            if (response != null)
            {
                raw.getWriter().write("data: " + toJson(response) + "\n\n");
                raw.flushBuffer();
            }
        }
        catch (Exception e)
        {
            String escaped = escapeJson(e.getMessage());
            raw.getWriter().write(
                    "data: {\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32603,\"message\":\"" + escaped + "\"}}\n\n");
            raw.flushBuffer();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json)
    {
        return JsonUtils.getMapFromJson(json);
    }

    private String toJson(Object obj)
    {
        return JSONB.toJson(obj);
    }

    private String escapeJson(String s)
    {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private Map<String, Object> createErrorResponse(int code, String message)
    {
        Map<String, Object> error = new HashMap<>();
        error.put("jsonrpc", "2.0");
        Map<String, Object> err = new HashMap<>();
        err.put("code", code);
        err.put("message", message);
        error.put("error", err);
        return error;
    }
}