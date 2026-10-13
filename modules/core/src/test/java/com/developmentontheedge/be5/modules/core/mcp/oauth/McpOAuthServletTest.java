package com.developmentontheedge.be5.modules.core.mcp.oauth;

import com.developmentontheedge.be5.modules.core.mcp.HttpMock;
import com.developmentontheedge.be5.modules.core.mcp.McpAuthenticationService;
import com.developmentontheedge.be5.modules.core.mcp.McpService;
import com.developmentontheedge.be5.modules.core.mcp.McpServlet;
import com.developmentontheedge.be5.modules.core.services.LoginService;
import com.developmentontheedge.be5.security.UserInfo;
import com.developmentontheedge.be5.server.SessionConstants;
import com.developmentontheedge.be5.server.authentication.RoleService;
import com.developmentontheedge.be5.util.JsonUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class McpOAuthServletTest
{
    private static final String REDIRECT_URI = "https://claude.ai/api/mcp/auth_callback";
    // the example of RFC 7636, appendix B
    private static final String VERIFIER = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
    private static final String CHALLENGE = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";

    private McpOAuthServlet servlet;
    private McpServlet mcpServlet;
    private RoleService roleService;

    @Before
    public void setUp()
    {
        LoginService loginService = mock(LoginService.class);
        when(loginService.loginCheck(eq("anna"), eq("secret".toCharArray()))).thenReturn(true);
        roleService = mock(RoleService.class);
        when(roleService.getAvailableRoles("anna")).thenReturn(Collections.singletonList("Administrator"));
        when(roleService.getCurrentRoles("anna")).thenReturn(Collections.singletonList("Administrator"));

        OAuthTokenStore store = new OAuthTokenStore();
        McpAuthenticationService authService = new McpAuthenticationService(loginService, roleService, store);
        servlet = new McpOAuthServlet(store, authService);

        McpService mcpService = mock(McpService.class);
        when(mcpService.handleRequest(any())).thenReturn(Collections.singletonMap("jsonrpc", "2.0"));
        mcpServlet = new McpServlet(mcpService, authService);
    }

    @After
    public void tearDown()
    {
        System.clearProperty(McpOAuthConfig.ISSUER_PROPERTY);
        System.clearProperty(McpOAuthConfig.CLIENTS_PROPERTY);
        System.clearProperty(McpOAuthConfig.ROLES_PROPERTY);
    }

    @Test
    public void unauthorizedRequestPointsAtTheMetadata() throws Exception
    {
        HttpMock http = new HttpMock("POST", "/api/mcp").body("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\"}");
        mcpServlet.service(http.request, http.response);

        assertEquals(401, http.status);
        assertEquals("Bearer resource_metadata=\"https://be5.example.org/api/oauth/.well-known/oauth-protected-resource\"",
                http.headers.get("WWW-Authenticate"));
    }

    @Test
    public void metadata() throws Exception
    {
        Map<String, Object> resource = json(get("/api/oauth/.well-known/oauth-protected-resource"));
        assertEquals("https://be5.example.org/api/mcp", resource.get("resource"));
        assertEquals(Collections.singletonList("https://be5.example.org/api/oauth"), resource.get("authorization_servers"));

        for (String path : new String[]{"/api/oauth/.well-known/oauth-authorization-server",
                "/api/oauth/.well-known/openid-configuration", "/.well-known/oauth-authorization-server/api/oauth"})
        {
            Map<String, Object> server = json(get(path));
            assertEquals("https://be5.example.org/api/oauth", server.get("issuer"));
            assertEquals("https://be5.example.org/api/oauth/authorize", server.get("authorization_endpoint"));
            assertEquals("https://be5.example.org/api/oauth/token", server.get("token_endpoint"));
            assertEquals("https://be5.example.org/api/oauth/register", server.get("registration_endpoint"));
            assertEquals(Collections.singletonList("S256"), server.get("code_challenge_methods_supported"));
        }
    }

    @Test
    public void issuerBehindProxyAndConfigured() throws Exception
    {
        HttpMock http = new HttpMock("GET", "/app/api/oauth/.well-known/oauth-protected-resource")
                .header("Host", "10.0.0.5:8080").header("X-Forwarded-Host", "public.example.org, 10.0.0.1")
                .header("X-Forwarded-Proto", "https");
        when(http.request.getContextPath()).thenReturn("/app");
        servlet.service(http.request, http.response);
        assertEquals("https://public.example.org/app/api/mcp", json(http).get("resource"));

        http = new HttpMock("GET", "/api/oauth/.well-known/oauth-protected-resource").header("Host", "localhost:8200");
        servlet.service(http.request, http.response);
        assertEquals("http://localhost:8200/api/mcp", json(http).get("resource"));

        System.setProperty(McpOAuthConfig.ISSUER_PROPERTY, "https://pinned.example.org/api/oauth/");
        assertEquals("https://pinned.example.org/api/mcp",
                json(get("/api/oauth/.well-known/oauth-protected-resource")).get("resource"));
    }

    @Test
    public void fullFlowWithLoginForm() throws Exception
    {
        String clientId = register();

        HttpMock form = authorize("GET", clientId);
        assertEquals(200, form.status);
        assertTrue(form.body().contains("name=\"password\""));
        assertTrue(form.body().contains("Claude &lt;test&gt;"));
        assertEquals("DENY", form.headers.get("X-Frame-Options"));

        HttpMock wrong = authorize("POST", clientId).param("username", "anna").param("password", "wrong");
        servlet.service(wrong.request, wrong.response);
        assertEquals(200, wrong.status);
        assertNull(wrong.headers.get("Location"));

        String code = login(clientId);

        Map<String, Object> tokens = json(token(clientId, code, VERIFIER));
        String accessToken = (String) tokens.get("access_token");
        assertEquals("Bearer", tokens.get("token_type"));
        assertNotNull(tokens.get("refresh_token"));
        assertEquals(200, callMcp(accessToken).status);

        // a code works once
        assertEquals(400, token(clientId, code, VERIFIER).status);

        HttpMock refresh = new HttpMock("POST", "/api/oauth/token").param("grant_type", "refresh_token")
                .param("refresh_token", (String) tokens.get("refresh_token")).param("client_id", clientId);
        servlet.service(refresh.request, refresh.response);
        String refreshed = (String) json(refresh).get("access_token");
        assertNotEquals(accessToken, refreshed);
        assertEquals(200, callMcp(refreshed).status);

        assertEquals(401, callMcp("mcp_at_forged").status);
    }

    @Test
    public void tokenRequestMustMatchTheCode() throws Exception
    {
        String clientId = register();

        assertEquals(400, token(clientId, login(clientId), "wrong-verifier").status);
        assertEquals(400, token(register(), login(clientId), VERIFIER).status);

        HttpMock http = new HttpMock("POST", "/api/oauth/token").param("grant_type", "authorization_code")
                .param("code", login(clientId)).param("client_id", clientId).param("code_verifier", VERIFIER)
                .param("redirect_uri", "https://evil.example.org/callback");
        servlet.service(http.request, http.response);
        assertEquals(400, http.status);
        assertEquals("invalid_grant", json(http).get("error"));
    }

    @Test
    public void authorizeRequestIsValidated() throws Exception
    {
        String clientId = register();

        assertEquals(400, authorize("GET", "unknown-client").status);
        assertEquals(400, authorize("GET", clientId, "redirect_uri", "https://evil.example.org/callback").status);
        assertEquals(400, authorize("GET", clientId, "code_challenge_method", "plain").status);
        assertEquals(400, authorize("GET", clientId, "code_challenge", "short").status);
        assertEquals(400, authorize("GET", clientId, "resource", "https://other.example.org/api/else").status);
        // credentials are not taken from a link
        HttpMock get = authorize("GET", clientId, "username", "anna", "password", "secret");
        assertNull(get.headers.get("Location"));
    }

    @Test
    public void signedInUserMustConsent() throws Exception
    {
        String clientId = register();
        UserInfo anna = new UserInfo("anna", Collections.singletonList("Administrator"),
                Collections.singletonList("Administrator"));

        HttpMock page = authorize("GET", clientId, anna);
        assertNull(page.headers.get("Location"));
        assertTrue(page.body().contains("Signed in as <b>anna</b>"));
        Matcher matcher = Pattern.compile("name=\"consent_token\" value=\"([^\"]+)\"").matcher(page.body());
        assertTrue(matcher.find());
        String consentToken = matcher.group(1);

        // an approval without the token of the rendered page is not accepted
        HttpMock forged = authorize("POST", clientId, anna, "approve", "1", "consent_token", "mcp_cs_forged");
        assertNull(forged.headers.get("Location"));

        // nor the token of another session
        HttpMock other = authorizeRequest("POST", clientId, "approve", "1", "consent_token", consentToken)
                .session("other-session", SessionConstants.USER_INFO, anna);
        servlet.service(other.request, other.response);
        assertNull(other.headers.get("Location"));

        String next = consentToken(authorize("GET", clientId, anna));
        HttpMock approved = authorize("POST", clientId, anna, "approve", "1", "consent_token", next);
        assertEquals(302, approved.status);
        assertTrue(approved.headers.get("Location").startsWith(REDIRECT_URI + "?code=mcp_ac_"));
        assertTrue(approved.headers.get("Location").endsWith("&state=xyz%2F1"));

        HttpMock denied = authorize("POST", clientId, anna, "deny", "1", "consent_token",
                consentToken(authorize("GET", clientId, anna)));
        assertEquals(REDIRECT_URI + "?error=access_denied&state=xyz%2F1", denied.headers.get("Location"));
    }

    @Test
    public void guestSessionGetsTheLoginForm() throws Exception
    {
        UserInfo guest = new UserInfo("Guest", Collections.singletonList("Guest"), Collections.singletonList("Guest"));
        HttpMock page = authorize("GET", register(), guest);

        assertTrue(page.body().contains("name=\"password\""));
        assertFalse(page.body().contains("consent_token"));
    }

    @Test
    public void rolesCanBeRestricted() throws Exception
    {
        String clientId = register();
        Map<String, Object> tokens = json(token(clientId, login(clientId), VERIFIER));

        System.setProperty(McpOAuthConfig.ROLES_PROPERTY, "SystemDeveloper, Auditor");
        HttpMock http = authorize("POST", clientId).param("username", "anna").param("password", "secret");
        servlet.service(http.request, http.response);
        assertNull(http.headers.get("Location"));

        // the roles are checked again when a token is refreshed
        HttpMock refresh = new HttpMock("POST", "/api/oauth/token").param("grant_type", "refresh_token")
                .param("refresh_token", (String) tokens.get("refresh_token"));
        servlet.service(refresh.request, refresh.response);
        assertEquals(400, refresh.status);

        when(roleService.getAvailableRoles("anna")).thenReturn(Collections.singletonList("Auditor"));
        assertNotNull(login(clientId));
    }

    @Test
    public void registrationChecksRedirectUris() throws Exception
    {
        assertNull(McpOAuthServlet.checkRedirectUri("https://claude.ai/api/mcp/auth_callback"));
        assertNull(McpOAuthServlet.checkRedirectUri("http://localhost:33418/callback"));
        assertNull(McpOAuthServlet.checkRedirectUri("http://127.0.0.1:6274/oauth/callback"));
        assertNull(McpOAuthServlet.checkRedirectUri("com.example.app:/callback"));
        assertNotNull(McpOAuthServlet.checkRedirectUri("http://example.org/callback"));
        assertNotNull(McpOAuthServlet.checkRedirectUri("javascript:alert(1)"));
        assertNotNull(McpOAuthServlet.checkRedirectUri("https://example.org/callback#fragment"));
        assertNotNull(McpOAuthServlet.checkRedirectUri("https://user@example.org/callback"));
        assertNotNull(McpOAuthServlet.checkRedirectUri("/relative"));

        HttpMock http = new HttpMock("POST", "/api/oauth/register").body("{\"redirect_uris\":[\"http://example.org/\"]}");
        servlet.service(http.request, http.response);
        assertEquals(400, http.status);
        assertEquals("invalid_redirect_uri", json(http).get("error"));

        http = new HttpMock("POST", "/api/oauth/register").body("not json");
        servlet.service(http.request, http.response);
        assertEquals(400, http.status);
    }

    @Test
    public void staticClient() throws Exception
    {
        System.setProperty(McpOAuthConfig.CLIENTS_PROPERTY, "fixed=" + REDIRECT_URI + "|https://example.org/cb;other=x:/y");

        assertNotNull(login("fixed"));
        assertEquals(400, authorize("GET", "other").status);
    }

    private HttpMock get(String path) throws Exception
    {
        HttpMock http = new HttpMock("GET", path);
        servlet.service(http.request, http.response);
        assertEquals(200, http.status);
        return http;
    }

    private String register() throws Exception
    {
        HttpMock http = new HttpMock("POST", "/api/oauth/register")
                .body("{\"client_name\":\"Claude <test>\",\"redirect_uris\":[\"" + REDIRECT_URI + "\"],"
                        + "\"token_endpoint_auth_method\":\"none\"}");
        servlet.service(http.request, http.response);
        assertEquals(201, http.status);
        Map<String, Object> client = json(http);
        assertEquals("none", client.get("token_endpoint_auth_method"));
        return (String) client.get("client_id");
    }

    private HttpMock authorizeRequest(String method, String clientId, String... more) throws Exception
    {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("response_type", "code");
        params.put("client_id", clientId);
        params.put("redirect_uri", REDIRECT_URI);
        params.put("state", "xyz/1");
        params.put("code_challenge", CHALLENGE);
        params.put("code_challenge_method", "S256");
        params.put("resource", "https://be5.example.org/api/mcp");
        for (int i = 0; i < more.length; i += 2) params.put(more[i], more[i + 1]);
        return new HttpMock(method, "/api/oauth/authorize").params(params);
    }

    private HttpMock authorize(String method, String clientId, String... more) throws Exception
    {
        HttpMock http = authorizeRequest(method, clientId, more);
        if ("GET".equals(method)) servlet.service(http.request, http.response);
        return http;
    }

    private HttpMock authorize(String method, String clientId, UserInfo sessionUser, String... more) throws Exception
    {
        HttpMock http = authorizeRequest(method, clientId, more).session("session-1", SessionConstants.USER_INFO, sessionUser);
        servlet.service(http.request, http.response);
        return http;
    }

    private static String consentToken(HttpMock page)
    {
        Matcher matcher = Pattern.compile("name=\"consent_token\" value=\"([^\"]+)\"").matcher(page.body());
        assertTrue(matcher.find());
        return matcher.group(1);
    }

    /** Signs in through the login form and returns the authorization code. */
    private String login(String clientId) throws Exception
    {
        HttpMock http = authorize("POST", clientId).param("username", "anna").param("password", "secret");
        servlet.service(http.request, http.response);
        assertEquals(302, http.status);
        String location = http.headers.get("Location");
        assertTrue(location, location.startsWith(REDIRECT_URI + "?code="));
        assertTrue(location, location.endsWith("&state=xyz%2F1"));
        return URLDecoder.decode(location.substring(location.indexOf("code=") + 5, location.indexOf("&state")), "UTF-8");
    }

    private HttpMock token(String clientId, String code, String verifier) throws Exception
    {
        HttpMock http = new HttpMock("POST", "/api/oauth/token").param("grant_type", "authorization_code")
                .param("code", code).param("client_id", clientId).param("redirect_uri", REDIRECT_URI)
                .param("code_verifier", verifier);
        servlet.service(http.request, http.response);
        return http;
    }

    private HttpMock callMcp(String accessToken) throws Exception
    {
        HttpMock http = new HttpMock("POST", "/api/mcp").header("Authorization", "Bearer " + accessToken)
                .body("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\"}");
        mcpServlet.service(http.request, http.response);
        return http;
    }

    private static Map<String, Object> json(HttpMock http)
    {
        return new HashMap<>(JsonUtils.getMapFromJson(http.body()));
    }
}
