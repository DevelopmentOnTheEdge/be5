package com.developmentontheedge.be5.modules.core.mcp.oauth;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Configuration of the MCP OAuth 2.1 authorization server (see {@link McpOAuthServlet}).
 * All values are system properties, so a deployment can pin its public URL without code changes:
 * <ul>
 * <li>{@value #ISSUER_PROPERTY} - public base URL of the authorization server,
 * e.g. {@code https://example.org/app/api/oauth}. Used verbatim when set. Otherwise the issuer is derived
 * from the request: {@code X-Forwarded-Host}/{@code X-Forwarded-Proto} when present, else the {@code Host} header.</li>
 * <li>{@value #CLIENTS_PROPERTY} - static client allow-list, {@code clientId=uri1|uri2;clientId2=uri3}.
 * Only needed for clients with a fixed id; others register themselves (RFC 7591).</li>
 * <li>{@value #ROLES_PROPERTY} - comma-separated roles allowed to use MCP. Unset: any user that can log in.</li>
 * </ul>
 */
public final class McpOAuthConfig
{
    public static final String ISSUER_PROPERTY = "be5.mcp.oauth.issuer";
    public static final String CLIENTS_PROPERTY = "be5.mcp.oauth.clients";
    public static final String ROLES_PROPERTY = "be5.mcp.roles";

    public static final String MCP_PATH = "/api/mcp";
    public static final String OAUTH_PATH = "/api/oauth";

    private McpOAuthConfig()
    {
    }

    /**
     * The issuer (authorization server base URL) for this request, or null when no host is known.
     */
    public static String issuer(HttpServletRequest req)
    {
        String configured = System.getProperty(ISSUER_PROPERTY);
        if (configured != null && !configured.trim().isEmpty())
        {
            String base = configured.trim();
            while (base.endsWith("/")) base = base.substring(0, base.length() - 1);
            return base;
        }

        // the proxy-forwarded public host wins over the (possibly internal) Host header
        String host = firstValue(req.getHeader("X-Forwarded-Host"));
        if (host == null) host = firstValue(req.getHeader("Host"));
        if (host == null) return null;

        String proto = firstValue(req.getHeader("X-Forwarded-Proto"));
        if (proto != null) proto = proto.toLowerCase(Locale.ROOT);
        String scheme;
        if ("http".equals(proto) || "https".equals(proto)) scheme = proto;
        else if (isLoopback(hostName(host)) && req.getScheme() != null) scheme = req.getScheme();
        else scheme = "https";

        String contextPath = req.getContextPath() == null ? "" : req.getContextPath();
        return scheme + "://" + host + contextPath + OAUTH_PATH;
    }

    /**
     * The absolute URL of the MCP endpoint these tokens are issued for.
     */
    public static String resourceUrl(String issuer)
    {
        String base = issuer.endsWith(OAUTH_PATH) ? issuer.substring(0, issuer.length() - OAUTH_PATH.length()) : issuer;
        return base + MCP_PATH;
    }

    public static String protectedResourceMetadataUrl(String issuer)
    {
        return issuer + "/.well-known/oauth-protected-resource";
    }

    /**
     * Whether a {@code resource} indicator (RFC 8707) refers to the MCP endpoint of this application.
     * Only the path is compared: the host seen by the server may differ from the public one behind a proxy.
     */
    public static boolean isMcpResource(String resource)
    {
        try
        {
            String path = new URI(resource).getPath();
            if (path == null) return false;
            while (path.endsWith("/")) path = path.substring(0, path.length() - 1);
            return path.endsWith(MCP_PATH);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * The static client allow-list: client id to the exact redirect URIs it may use.
     */
    public static Map<String, List<String>> staticClients()
    {
        String raw = System.getProperty(CLIENTS_PROPERTY);
        if (raw == null || raw.trim().isEmpty()) return Collections.emptyMap();

        Map<String, List<String>> clients = new LinkedHashMap<>();
        for (String entry : raw.split(";"))
        {
            int eq = entry.indexOf('=');
            if (eq <= 0) continue;
            List<String> uris = new ArrayList<>();
            for (String uri : entry.substring(eq + 1).split("\\|"))
            {
                if (!uri.trim().isEmpty()) uris.add(uri.trim());
            }
            if (!uris.isEmpty()) clients.put(entry.substring(0, eq).trim(), uris);
        }
        return clients;
    }

    /**
     * Roles allowed to use MCP; empty means no restriction.
     */
    public static List<String> allowedRoles()
    {
        String raw = System.getProperty(ROLES_PROPERTY);
        if (raw == null || raw.trim().isEmpty()) return Collections.emptyList();

        List<String> roles = new ArrayList<>();
        for (String role : raw.split(","))
        {
            if (!role.trim().isEmpty()) roles.add(role.trim());
        }
        return roles;
    }

    static boolean isLoopback(String host)
    {
        if (host == null) return false;
        String h = host.toLowerCase(Locale.ROOT);
        return h.equals("localhost") || h.equals("[::1]") || h.equals("::1") || h.matches("127(\\.\\d{1,3}){3}");
    }

    private static String hostName(String hostAndPort)
    {
        if (hostAndPort.startsWith("["))
        {
            int end = hostAndPort.indexOf(']');
            return end > 0 ? hostAndPort.substring(0, end + 1) : hostAndPort;
        }
        int colon = hostAndPort.indexOf(':');
        return colon >= 0 ? hostAndPort.substring(0, colon) : hostAndPort;
    }

    /**
     * The first comma-separated value of a header (proxies chain several), or null when empty.
     */
    private static String firstValue(String value)
    {
        if (value == null) return null;
        String v = value;
        int comma = v.indexOf(',');
        if (comma >= 0) v = v.substring(0, comma);
        v = v.trim();
        return v.isEmpty() ? null : v;
    }
}
