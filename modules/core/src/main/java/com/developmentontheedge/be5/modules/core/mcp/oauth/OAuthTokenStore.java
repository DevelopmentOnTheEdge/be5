package com.developmentontheedge.be5.modules.core.mcp.oauth;

import com.developmentontheedge.be5.security.UserInfo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store of the MCP OAuth 2.1 authorization server: pending consents, one-time authorization codes,
 * access and refresh tokens, and dynamically registered clients.
 *
 * <p>Single-JVM by design: everything is lost on restart (clients then authorize again) and a multi-node
 * deployment would need a shared backend.</p>
 */
public class OAuthTokenStore
{
    public static final long ACCESS_TOKEN_TTL_MS = 60L * 60L * 1000L;
    public static final long REFRESH_TOKEN_TTL_MS = 7L * 24L * 60L * 60L * 1000L;
    public static final long AUTH_CODE_TTL_MS = 10L * 60L * 1000L;
    public static final long CONSENT_TTL_MS = 10L * 60L * 1000L;
    public static final long CLIENT_TTL_MS = 30L * 24L * 60L * 60L * 1000L;

    /** Registration is open to anyone, so the number of remembered clients is bounded. */
    static final int MAX_CLIENTS = 1000;

    public static final String TOKEN_PREFIX = "mcp_at_";
    public static final String REFRESH_PREFIX = "mcp_rt_";
    public static final String CODE_PREFIX = "mcp_ac_";
    public static final String CONSENT_PREFIX = "mcp_cs_";
    public static final String CLIENT_PREFIX = "mcp_cl_";

    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<String, Consent> consents = new ConcurrentHashMap<>();
    private final Map<String, AuthCode> codes = new ConcurrentHashMap<>();
    private final Map<String, AccessToken> tokens = new ConcurrentHashMap<>();
    private final Map<String, RefreshToken> refreshes = new ConcurrentHashMap<>();
    private final Map<String, RegisteredClient> clients = new ConcurrentHashMap<>();

    abstract static class Expiring
    {
        public final String value;
        public final long expiresAt;

        Expiring(String value, long ttlMs)
        {
            this.value = value;
            this.expiresAt = System.currentTimeMillis() + ttlMs;
        }

        boolean expired()
        {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    /** A client registered dynamically (RFC 7591) with the exact redirect URIs it may use. */
    public static final class RegisteredClient extends Expiring
    {
        public final List<String> redirectUris;
        public final String clientName;

        RegisteredClient(String clientId, List<String> redirectUris, String clientName)
        {
            super(clientId, CLIENT_TTL_MS);
            this.redirectUris = redirectUris;
            this.clientName = clientName;
        }
    }

    /**
     * Ties a consent page submission to the page this server rendered for a signed-in user:
     * the approval cannot be forged by a cross-site request nor replayed for another request.
     */
    public static final class Consent extends Expiring
    {
        private final String sessionId;
        private final String userName;
        private final String clientId;
        private final String redirectUri;
        private final String codeChallenge;

        Consent(String value, String sessionId, String userName, String clientId, String redirectUri, String codeChallenge)
        {
            super(value, CONSENT_TTL_MS);
            this.sessionId = sessionId;
            this.userName = userName;
            this.clientId = clientId;
            this.redirectUri = redirectUri;
            this.codeChallenge = codeChallenge;
        }

        public boolean matches(String sessionId, String userName, String clientId, String redirectUri, String codeChallenge)
        {
            return this.sessionId.equals(sessionId) && this.userName.equals(userName) && this.clientId.equals(clientId)
                    && this.redirectUri.equals(redirectUri) && this.codeChallenge.equals(codeChallenge);
        }
    }

    /** A one-time authorization code issued by {@code /authorize}. */
    public static final class AuthCode extends Expiring
    {
        public final String clientId;
        public final String redirectUri;
        public final String codeChallenge;
        public final String userName;

        AuthCode(String value, String clientId, String redirectUri, String codeChallenge, String userName)
        {
            super(value, AUTH_CODE_TTL_MS);
            this.clientId = clientId;
            this.redirectUri = redirectUri;
            this.codeChallenge = codeChallenge;
            this.userName = userName;
        }
    }

    /** An opaque bearer token; carries the user (with roles as of the moment it was issued). */
    public static final class AccessToken extends Expiring
    {
        public final String clientId;
        public final UserInfo user;

        AccessToken(String value, String clientId, UserInfo user)
        {
            super(value, ACCESS_TOKEN_TTL_MS);
            this.clientId = clientId;
            this.user = user;
        }
    }

    /** Lets a client get a new access token without running the authorization flow again. */
    public static final class RefreshToken extends Expiring
    {
        public final String clientId;
        public final String userName;

        RefreshToken(String value, String clientId, String userName)
        {
            super(value, REFRESH_TOKEN_TTL_MS);
            this.clientId = clientId;
            this.userName = userName;
        }
    }

    public Consent issueConsent(String sessionId, String userName, String clientId, String redirectUri,
                                String codeChallenge)
    {
        purgeExpired();
        Consent consent = new Consent(CONSENT_PREFIX + randomToken(32), sessionId, userName, clientId, redirectUri,
                codeChallenge);
        consents.put(consent.value, consent);
        return consent;
    }

    /** Single use; null if unknown or expired. */
    public Consent consumeConsent(String value)
    {
        return value == null ? null : alive(consents.remove(value));
    }

    public AuthCode issueCode(String clientId, String redirectUri, String codeChallenge, String userName)
    {
        purgeExpired();
        AuthCode code = new AuthCode(CODE_PREFIX + randomToken(32), clientId, redirectUri, codeChallenge, userName);
        codes.put(code.value, code);
        return code;
    }

    /** Single use; null if unknown or expired. */
    public AuthCode consumeCode(String value)
    {
        return value == null ? null : alive(codes.remove(value));
    }

    public AccessToken issueAccessToken(String clientId, UserInfo user)
    {
        AccessToken token = new AccessToken(TOKEN_PREFIX + randomToken(32), clientId, user);
        tokens.put(token.value, token);
        return token;
    }

    public RefreshToken issueRefreshToken(String clientId, String userName)
    {
        RefreshToken token = new RefreshToken(REFRESH_PREFIX + randomToken(32), clientId, userName);
        refreshes.put(token.value, token);
        return token;
    }

    /** The access token record, or null if unknown or expired. */
    public AccessToken validate(String value)
    {
        return value == null ? null : alive(tokens.get(value));
    }

    /** The refresh token record, or null if unknown or expired. It stays valid: refresh tokens are not rotated. */
    public RefreshToken findRefreshToken(String value)
    {
        return value == null ? null : alive(refreshes.get(value));
    }

    public void revokeRefreshToken(String value)
    {
        if (value != null) refreshes.remove(value);
    }

    /** The client id is generated here, so a client cannot pick a confusing one. */
    public RegisteredClient registerClient(List<String> redirectUris, String clientName)
    {
        purgeExpired();
        Iterator<RegisteredClient> it = clients.values().iterator();
        while (clients.size() >= MAX_CLIENTS && it.hasNext())
        {
            it.next();
            it.remove();
        }
        RegisteredClient client = new RegisteredClient(CLIENT_PREFIX + randomToken(24), redirectUris, clientName);
        clients.put(client.value, client);
        return client;
    }

    public RegisteredClient findClient(String clientId)
    {
        return clientId == null ? null : alive(clients.get(clientId));
    }

    /**
     * Null when the client may use the redirect URI, otherwise the reason. The URI must equal a registered one
     * exactly (open-redirect defense). Dynamically registered clients are looked up first, then the static list.
     */
    public String checkClient(String clientId, String redirectUri)
    {
        if (clientId == null || clientId.isEmpty()) return "missing client_id";

        List<String> uris;
        RegisteredClient dynamic = findClient(clientId);
        if (dynamic != null) uris = dynamic.redirectUris;
        else uris = McpOAuthConfig.staticClients().get(clientId);

        if (uris == null) return "unknown client_id";
        if (redirectUri == null || !uris.contains(redirectUri)) return "redirect_uri not registered for this client";
        return null;
    }

    /** The S256 code challenge of a verifier: BASE64URL(SHA-256(verifier)), RFC 7636. */
    public static String pkceChallenge(String verifier)
    {
        try
        {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(sha.digest(verifier.getBytes(StandardCharsets.US_ASCII)));
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException(e);
        }
    }

    public static boolean pkceMatches(String challenge, String verifier)
    {
        if (challenge == null || verifier == null || verifier.isEmpty()) return false;
        return MessageDigest.isEqual(challenge.getBytes(StandardCharsets.US_ASCII),
                pkceChallenge(verifier).getBytes(StandardCharsets.US_ASCII));
    }

    private static <T extends Expiring> T alive(T item)
    {
        return item == null || item.expired() ? null : item;
    }

    private static String randomToken(int bytes)
    {
        byte[] buf = new byte[bytes];
        RANDOM.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    void purgeExpired()
    {
        consents.values().removeIf(Expiring::expired);
        codes.values().removeIf(Expiring::expired);
        tokens.values().removeIf(Expiring::expired);
        refreshes.values().removeIf(Expiring::expired);
        clients.values().removeIf(Expiring::expired);
    }
}
