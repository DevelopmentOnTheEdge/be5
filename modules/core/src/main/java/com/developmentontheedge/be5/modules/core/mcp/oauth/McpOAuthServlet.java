package com.developmentontheedge.be5.modules.core.mcp.oauth;

import com.developmentontheedge.be5.modules.core.mcp.McpAuthenticationService;
import com.developmentontheedge.be5.security.UserInfo;
import com.developmentontheedge.be5.server.SessionConstants;
import com.developmentontheedge.be5.util.JsonUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The OAuth 2.1 authorization server of the MCP endpoint. Remote MCP clients (Claude custom connectors and others)
 * cannot present a password up front: they get a 401 challenge from {@code /api/mcp}, read the metadata served here
 * and run the authorization-code flow with PKCE, in which the user signs in with the be5 account.
 *
 * <ul>
 * <li>{@code GET /api/oauth/.well-known/oauth-protected-resource} - RFC 9728 metadata of the MCP endpoint.</li>
 * <li>{@code GET /api/oauth/.well-known/oauth-authorization-server} (also {@code openid-configuration}) -
 * RFC 8414 metadata.</li>
 * <li>{@code POST /api/oauth/register} - dynamic client registration (RFC 7591), public clients only.</li>
 * <li>{@code GET|POST /api/oauth/authorize} - the user step: a consent page for a signed-in user, else a login form.
 * A code is issued only on a submission of one of these pages.</li>
 * <li>{@code POST /api/oauth/token} - {@code authorization_code} and {@code refresh_token} grants.</li>
 * </ul>
 */
@Singleton
public class McpOAuthServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(McpOAuthServlet.class.getName());
    private static final Jsonb JSONB = JsonbBuilder.create();

    static final int MAX_REDIRECT_URIS = 10;

    private final OAuthTokenStore store;
    private final McpAuthenticationService authService;

    @Inject
    public McpOAuthServlet(OAuthTokenStore store, McpAuthenticationService authService)
    {
        this.store = store;
        this.authService = authService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        route(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        route(req, resp);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
    {
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, MCP-Protocol-Version");
        resp.setHeader("Access-Control-Max-Age", "86400");
    }

    private void route(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        // a form does not name its encoding, and the container would read a password as ISO-8859-1
        if (req.getCharacterEncoding() == null) req.setCharacterEncoding("UTF-8");
        String path = req.getRequestURI();
        boolean post = "POST".equals(req.getMethod());
        try
        {
            if (path.contains("/.well-known/oauth-protected-resource")) protectedResource(req, resp);
            else if (path.contains("/.well-known/")) authorizationServer(req, resp);
            else if (path.endsWith("/authorize")) authorize(req, resp);
            else if (path.endsWith("/token") && post) token(req, resp);
            else if (path.endsWith("/register") && post) register(req, resp);
            else sendError(resp, HttpServletResponse.SC_NOT_FOUND, "invalid_request", "unknown OAuth endpoint");
        }
        catch (RuntimeException e)
        {
            log.log(Level.SEVERE, "MCP OAuth endpoint failed: " + path, e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "server_error", "internal error");
        }
    }

    private void protectedResource(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        String issuer = McpOAuthConfig.issuer(req);
        if (issuer == null)
        {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "server_error", "cannot determine the issuer");
            return;
        }
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("resource", McpOAuthConfig.resourceUrl(issuer));
        doc.put("authorization_servers", Collections.singletonList(issuer));
        doc.put("bearer_methods_supported", Collections.singletonList("header"));
        sendJson(resp, HttpServletResponse.SC_OK, doc);
    }

    private void authorizationServer(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        String issuer = McpOAuthConfig.issuer(req);
        if (issuer == null)
        {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "server_error", "cannot determine the issuer");
            return;
        }
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("issuer", issuer);
        doc.put("authorization_endpoint", issuer + "/authorize");
        doc.put("token_endpoint", issuer + "/token");
        doc.put("registration_endpoint", issuer + "/register");
        doc.put("response_types_supported", Collections.singletonList("code"));
        doc.put("grant_types_supported", Arrays.asList("authorization_code", "refresh_token"));
        doc.put("code_challenge_methods_supported", Collections.singletonList("S256"));
        doc.put("token_endpoint_auth_methods_supported", Collections.singletonList("none"));
        doc.put("scopes_supported", Collections.emptyList());
        sendJson(resp, HttpServletResponse.SC_OK, doc);
    }

    /**
     * Never redirects to the client without an explicit action of the user on a page rendered here: a signed-in
     * user gets a consent page (so a link to /authorize with an attacker's client yields a page, not a code),
     * anyone else the login form. All request values are validated before anything is rendered or issued.
     */
    private void authorize(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        AuthorizeRequest auth = new AuthorizeRequest(req);
        String invalid = validate(auth);
        if (invalid != null)
        {
            sendHtml(resp, HttpServletResponse.SC_BAD_REQUEST, errorPage(invalid));
            return;
        }
        boolean post = "POST".equals(req.getMethod());

        HttpSession session = req.getSession(false);
        UserInfo sessionUser = session == null ? null : (UserInfo) session.getAttribute(SessionConstants.USER_INFO);
        if (authService.isPermitted(sessionUser))
        {
            String userName = sessionUser.getUserName();
            OAuthTokenStore.Consent consent = post ? store.consumeConsent(param(req, "consent_token")) : null;
            if (consent == null
                    || !consent.matches(session.getId(), userName, auth.clientId, auth.redirectUri, auth.challenge))
            {
                String token = store.issueConsent(session.getId(), userName, auth.clientId, auth.redirectUri,
                        auth.challenge).value;
                sendHtml(resp, HttpServletResponse.SC_OK, consentPage(auth, userName, token));
            }
            else if (param(req, "approve").isEmpty())
            {
                redirect(resp, auth, "error=access_denied");
            }
            else
            {
                issueCode(resp, auth, userName);
            }
            return;
        }

        String userName = param(req, "username");
        String password = param(req, "password");
        if (!post || (userName.isEmpty() && password.isEmpty()))
        {
            sendHtml(resp, HttpServletResponse.SC_OK, loginPage(auth, null));
            return;
        }
        UserInfo user = authService.login(userName, password, req.getRemoteAddr());
        if (user == null)
        {
            sendHtml(resp, HttpServletResponse.SC_OK, loginPage(auth, "Login failed, or this account may not use MCP."));
            return;
        }
        issueCode(resp, auth, user.getUserName());
    }

    private String validate(AuthorizeRequest auth)
    {
        String invalid = store.checkClient(auth.clientId, auth.redirectUri);
        if (invalid != null) return invalid;
        if (!auth.responseType.isEmpty() && !"code".equals(auth.responseType)) return "response_type must be code";
        if (!"S256".equals(auth.challengeMethod)) return "PKCE is required: code_challenge_method must be S256";
        // BASE64URL(SHA-256(verifier)) is exactly 43 characters
        if (!auth.challenge.matches("[A-Za-z0-9_-]{43}")) return "code_challenge must be a base64url SHA-256 digest";
        if (!auth.resource.isEmpty() && !McpOAuthConfig.isMcpResource(auth.resource)) return "unsupported resource";
        return null;
    }

    private void issueCode(HttpServletResponse resp, AuthorizeRequest auth, String userName)
    {
        OAuthTokenStore.AuthCode code = store.issueCode(auth.clientId, auth.redirectUri, auth.challenge, userName);
        log.info("MCP OAuth code issued user=" + userName + " client=" + auth.clientId);
        redirect(resp, auth, "code=" + urlEncode(code.value));
    }

    private static void redirect(HttpServletResponse resp, AuthorizeRequest auth, String query)
    {
        String location = auth.redirectUri + (auth.redirectUri.indexOf('?') >= 0 ? "&" : "?") + query
                + (auth.state.isEmpty() ? "" : "&state=" + urlEncode(auth.state));
        resp.setStatus(HttpServletResponse.SC_FOUND);
        resp.setHeader("Location", location);
        resp.setHeader("Cache-Control", "no-store");
    }

    private void token(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        String grantType = param(req, "grant_type");
        if ("refresh_token".equals(grantType))
        {
            refresh(req, resp);
            return;
        }
        if (!"authorization_code".equals(grantType))
        {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "unsupported_grant_type",
                    "only authorization_code and refresh_token are supported");
            return;
        }

        OAuthTokenStore.AuthCode code = store.consumeCode(param(req, "code"));
        String invalid = null;
        if (code == null) invalid = "unknown or expired authorization code";
        else if (!code.clientId.equals(param(req, "client_id"))) invalid = "client_id does not match the code";
        else if (!code.redirectUri.equals(param(req, "redirect_uri"))) invalid = "redirect_uri does not match the code";
        else if (!OAuthTokenStore.pkceMatches(code.codeChallenge, param(req, "code_verifier")))
        {
            invalid = "code_verifier does not satisfy the code_challenge";
        }
        if (invalid != null)
        {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "invalid_grant", invalid);
            return;
        }

        UserInfo user = authService.createUserInfo(code.userName, req.getRemoteAddr());
        if (user == null)
        {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "invalid_grant", "the user may not use MCP");
            return;
        }
        String refreshToken = store.issueRefreshToken(code.clientId, code.userName).value;
        log.info("MCP OAuth token issued user=" + code.userName + " client=" + code.clientId);
        sendTokens(resp, store.issueAccessToken(code.clientId, user), refreshToken);
    }

    /**
     * Exchanges a refresh token for a new access token, so a connected client stays signed in past the lifetime
     * of an access token. The user's roles are read again; the refresh token itself is not rotated.
     */
    private void refresh(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        OAuthTokenStore.RefreshToken refreshToken = store.findRefreshToken(param(req, "refresh_token"));
        String clientId = param(req, "client_id");
        if (refreshToken == null || (!clientId.isEmpty() && !refreshToken.clientId.equals(clientId)))
        {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "invalid_grant", "unknown or expired refresh token");
            return;
        }
        UserInfo user = authService.createUserInfo(refreshToken.userName, req.getRemoteAddr());
        if (user == null)
        {
            store.revokeRefreshToken(refreshToken.value);
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "invalid_grant", "the user may not use MCP");
            return;
        }
        log.info("MCP OAuth token refreshed user=" + refreshToken.userName + " client=" + refreshToken.clientId);
        sendTokens(resp, store.issueAccessToken(refreshToken.clientId, user), refreshToken.value);
    }

    private void sendTokens(HttpServletResponse resp, OAuthTokenStore.AccessToken token, String refreshToken)
            throws IOException
    {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("access_token", token.value);
        body.put("token_type", "Bearer");
        body.put("expires_in", OAuthTokenStore.ACCESS_TOKEN_TTL_MS / 1000L);
        body.put("refresh_token", refreshToken);
        sendJson(resp, HttpServletResponse.SC_OK, body);
    }

    private void register(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        Map<String, Object> body;
        try
        {
            body = JsonUtils.getMapFromJson(readBody(req));
        }
        catch (RuntimeException e)
        {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "invalid_client_metadata", "the body is not valid JSON");
            return;
        }

        List<String> uris = new ArrayList<>();
        Object urisValue = body.get("redirect_uris");
        if (urisValue instanceof List)
        {
            for (Object uri : (List<?>) urisValue)
            {
                if (uri != null && !uri.toString().trim().isEmpty()) uris.add(uri.toString().trim());
            }
        }
        String invalid = null;
        if (uris.isEmpty()) invalid = "redirect_uris is required and must be a non-empty array";
        else if (uris.size() > MAX_REDIRECT_URIS) invalid = "at most " + MAX_REDIRECT_URIS + " redirect_uris are allowed";
        for (int i = 0; invalid == null && i < uris.size(); i++)
        {
            invalid = checkRedirectUri(uris.get(i));
        }
        if (invalid != null)
        {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "invalid_redirect_uri", invalid);
            return;
        }

        String clientName = body.get("client_name") == null ? "" : body.get("client_name").toString();
        if (clientName.length() > 200) clientName = clientName.substring(0, 200);
        OAuthTokenStore.RegisteredClient client = store.registerClient(uris, clientName);
        log.info("MCP OAuth client registered id=" + client.value + " name=" + clientName + " uris=" + uris);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("client_id", client.value);
        out.put("client_id_issued_at", System.currentTimeMillis() / 1000L);
        out.put("client_name", clientName);
        out.put("redirect_uris", uris);
        out.put("grant_types", Arrays.asList("authorization_code", "refresh_token"));
        out.put("response_types", Collections.singletonList("code"));
        out.put("token_endpoint_auth_method", "none");
        sendJson(resp, HttpServletResponse.SC_CREATED, out);
    }

    /**
     * Null when a redirect URI may be registered: https, http to a loopback host (native clients listen on
     * localhost), or a private-use scheme in reverse-domain form (RFC 8252). Anything else - javascript:, data:,
     * plain http to a remote host - would leak the authorization code.
     */
    static String checkRedirectUri(String uri)
    {
        if (uri.length() > 2048) return "redirect_uri is too long";
        URI parsed;
        try
        {
            parsed = new URI(uri);
        }
        catch (URISyntaxException e)
        {
            return "redirect_uri is not a valid URI";
        }
        String scheme = parsed.getScheme() == null ? "" : parsed.getScheme().toLowerCase(Locale.ROOT);
        if (scheme.isEmpty()) return "redirect_uri must be an absolute URI";
        if (parsed.getRawFragment() != null) return "redirect_uri must not contain a fragment";
        if (parsed.getRawUserInfo() != null) return "redirect_uri must not contain user info";
        String host = parsed.getHost();
        if ("https".equals(scheme)) return host == null || host.isEmpty() ? "https redirect_uri must have a host" : null;
        if ("http".equals(scheme))
        {
            return McpOAuthConfig.isLoopback(host) ? null : "http redirect_uri is only allowed for a loopback host";
        }
        if (scheme.indexOf('.') > 0) return null;
        return "unsupported redirect_uri scheme";
    }

    /** The parameters of an authorize request; the pages carry them along as hidden fields. */
    private static final class AuthorizeRequest
    {
        final String clientId;
        final String redirectUri;
        final String responseType;
        final String state;
        final String challenge;
        final String challengeMethod;
        final String resource;

        AuthorizeRequest(HttpServletRequest req)
        {
            clientId = param(req, "client_id");
            redirectUri = param(req, "redirect_uri");
            responseType = param(req, "response_type");
            state = param(req, "state");
            challenge = param(req, "code_challenge");
            challengeMethod = param(req, "code_challenge_method");
            resource = param(req, "resource");
        }
    }

    private static StringBuilder pageStart()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"utf-8\">\n");
        sb.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
        sb.append("<title>MCP sign-in</title>\n");
        sb.append("<style>body{font-family:sans-serif;max-width:28rem;margin:3rem auto;padding:0 1rem}");
        sb.append("input{width:100%;margin:0.3rem 0;padding:0.4rem;box-sizing:border-box}");
        sb.append("button{margin-top:0.8rem;margin-right:0.5rem;padding:0.4rem 1rem}");
        sb.append("code{word-break:break-all}.error{color:#a00}</style>\n</head>\n<body>\n");
        sb.append("<h2>MCP sign-in</h2>\n");
        return sb;
    }

    private static String errorPage(String error)
    {
        return pageStart().append("<p class=\"error\">").append(escape(error)).append("</p>\n</body>\n</html>\n")
                .toString();
    }

    /**
     * Who is asking and where the user will be sent. The redirect address is what the user should judge by:
     * the client name is self-asserted at registration.
     */
    private void appendClientInfo(StringBuilder sb, AuthorizeRequest auth)
    {
        OAuthTokenStore.RegisteredClient dynamic = store.findClient(auth.clientId);
        String name = dynamic != null && !dynamic.clientName.isEmpty() ? dynamic.clientName : auth.clientId;
        sb.append("<p>The application <b>").append(escape(name)).append("</b>");
        if (dynamic != null) sb.append(" (self-registered)");
        sb.append(" is requesting access to your account. It will be able to read data on your behalf.</p>\n");
        sb.append("<p>After you allow it, you will be sent to<br><code>").append(escape(auth.redirectUri))
                .append("</code></p>\n");
        sb.append("<p>Only continue if you started this connection yourself and recognize this address.</p>\n");
    }

    private static void appendForm(StringBuilder sb, AuthorizeRequest auth)
    {
        sb.append("<form method=\"post\">\n");
        hidden(sb, "client_id", auth.clientId);
        hidden(sb, "redirect_uri", auth.redirectUri);
        hidden(sb, "response_type", auth.responseType);
        hidden(sb, "state", auth.state);
        hidden(sb, "code_challenge", auth.challenge);
        hidden(sb, "code_challenge_method", auth.challengeMethod);
        hidden(sb, "resource", auth.resource);
    }

    private static void hidden(StringBuilder sb, String name, String value)
    {
        if (value.isEmpty()) return;
        sb.append("<input type=\"hidden\" name=\"").append(name).append("\" value=\"").append(escape(value))
                .append("\">\n");
    }

    private String loginPage(AuthorizeRequest auth, String error)
    {
        StringBuilder sb = pageStart();
        appendClientInfo(sb, auth);
        if (error != null) sb.append("<p class=\"error\">").append(escape(error)).append("</p>\n");
        appendForm(sb, auth);
        sb.append("<label>Username<br><input type=\"text\" name=\"username\" autocomplete=\"username\"></label>\n");
        sb.append("<label>Password<br><input type=\"password\" name=\"password\" autocomplete=\"current-password\">")
                .append("</label>\n");
        sb.append("<button type=\"submit\">Sign in and allow</button>\n</form>\n</body>\n</html>\n");
        return sb.toString();
    }

    private String consentPage(AuthorizeRequest auth, String userName, String consentToken)
    {
        StringBuilder sb = pageStart();
        sb.append("<p>Signed in as <b>").append(escape(userName)).append("</b>.</p>\n");
        appendClientInfo(sb, auth);
        appendForm(sb, auth);
        hidden(sb, "consent_token", consentToken);
        sb.append("<button type=\"submit\" name=\"approve\" value=\"1\">Allow</button>\n");
        sb.append("<button type=\"submit\" name=\"deny\" value=\"1\">Deny</button>\n</form>\n</body>\n</html>\n");
        return sb.toString();
    }

    private static String param(HttpServletRequest req, String name)
    {
        String value = req.getParameter(name);
        return value == null ? "" : value;
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

    private static void sendJson(HttpServletResponse resp, int status, Map<String, Object> body) throws IOException
    {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.getWriter().write(JSONB.toJson(body));
    }

    private static void sendError(HttpServletResponse resp, int status, String error, String description)
            throws IOException
    {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", error);
        body.put("error_description", description);
        sendJson(resp, status, body);
    }

    /** The pages may not be framed (clickjacking) nor cached (the consent page carries a one-time token). */
    private static void sendHtml(HttpServletResponse resp, int status, String html) throws IOException
    {
        resp.setStatus(status);
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("X-Frame-Options", "DENY");
        resp.setHeader("Content-Security-Policy", "frame-ancestors 'none'");
        resp.setHeader("Cache-Control", "no-store");
        resp.setHeader("Referrer-Policy", "no-referrer");
        resp.getWriter().write(html);
    }

    private static String escape(String s)
    {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String urlEncode(String s)
    {
        try
        {
            return URLEncoder.encode(s, "UTF-8");
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }
}
