package com.developmentontheedge.be5.modules.core.mcp;

import com.developmentontheedge.be5.modules.core.mcp.oauth.McpOAuthConfig;
import com.developmentontheedge.be5.security.UserInfo;
import com.developmentontheedge.be5.security.UserInfoHolder;
import com.developmentontheedge.be5.util.JsonUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The MCP endpoint: streamable HTTP transport with plain JSON responses (no SSE stream, no server sessions).
 * Every request must carry an OAuth bearer token (see {@link com.developmentontheedge.be5.modules.core.mcp.oauth})
 * or Basic credentials; otherwise the answer is a 401 challenge from which a client discovers the OAuth flow.
 */
@Singleton
public class McpServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(McpServlet.class.getName());
    private static final Jsonb JSONB = JsonbBuilder.create();

    private final McpService mcpService;
    private final McpAuthenticationService authService;

    @Inject
    public McpServlet(McpService mcpService, McpAuthenticationService authService)
    {
        this.mcpService = mcpService;
        this.authService = authService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        addCorsHeaders(req, resp);
        UserInfo user = authService.authenticateUser(req.getHeader("Authorization"), req.getRemoteAddr());
        if (user == null)
        {
            sendUnauthorized(req, resp);
            return;
        }

        Map<String, Object> request;
        try
        {
            request = JsonUtils.getMapFromJson(readBody(req));
        }
        catch (RuntimeException e)
        {
            sendJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "{\"jsonrpc\":\"2.0\",\"id\":null,\"error\":{\"code\":-32700,\"message\":\"Parse error\"}}");
            return;
        }

        long start = System.currentTimeMillis();
        Map<String, Object> response;
        UserInfo previousUser = UserInfoHolder.getLoggedUser();
        UserInfoHolder.setLoggedUser(user);
        try
        {
            response = mcpService.handleRequest(request);
        }
        finally
        {
            UserInfoHolder.setLoggedUser(previousUser);
        }
        log.fine(() -> "MCP " + request.get("method") + " user=" + user.getUserName() + " "
                + (System.currentTimeMillis() - start) + "ms");

        if (response == null)
        {
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            return;
        }
        sendJson(resp, HttpServletResponse.SC_OK, JSONB.toJson(response));
    }

    /**
     * There is no server-to-client stream, which the protocol lets a server say with 405.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        addCorsHeaders(req, resp);
        if (authService.authenticateUser(req.getHeader("Authorization"), req.getRemoteAddr()) == null)
        {
            sendUnauthorized(req, resp);
            return;
        }
        resp.setHeader("Allow", "POST, OPTIONS");
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
    {
        resp.setHeader("Allow", "POST, OPTIONS");
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
    {
        addCorsHeaders(req, resp);
        resp.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers",
                "Content-Type, Accept, Authorization, MCP-Protocol-Version, Mcp-Session-Id");
        resp.setHeader("Access-Control-Max-Age", "86400");
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    /**
     * Browser based clients read the endpoint cross-origin. Cookies are never used for authentication here,
     * so any origin may be allowed.
     */
    private static void addCorsHeaders(HttpServletRequest req, HttpServletResponse resp)
    {
        if (req.getHeader("Origin") != null)
        {
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Expose-Headers", "WWW-Authenticate");
        }
    }

    private static void sendUnauthorized(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        String issuer = McpOAuthConfig.issuer(req);
        if (issuer != null)
        {
            resp.setHeader("WWW-Authenticate",
                    "Bearer resource_metadata=\"" + McpOAuthConfig.protectedResourceMetadataUrl(issuer) + "\"");
        }
        sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                "{\"jsonrpc\":\"2.0\",\"id\":null,\"error\":{\"code\":-32001,\"message\":\"Authentication required\"}}");
    }

    private static void sendJson(HttpServletResponse resp, int status, String json) throws IOException
    {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json);
    }

    private static String readBody(HttpServletRequest req) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        char[] buf = new char[4096];
        for (int n = reader.read(buf); n >= 0; n = reader.read(buf))
        {
            sb.append(buf, 0, n);
        }
        return sb.toString();
    }
}
