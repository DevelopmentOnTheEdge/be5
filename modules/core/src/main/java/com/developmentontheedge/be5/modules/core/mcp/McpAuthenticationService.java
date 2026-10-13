package com.developmentontheedge.be5.modules.core.mcp;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.modules.core.mcp.oauth.McpOAuthConfig;
import com.developmentontheedge.be5.modules.core.mcp.oauth.OAuthTokenStore;
import com.developmentontheedge.be5.modules.core.services.LoginService;
import com.developmentontheedge.be5.security.UserInfo;
import com.developmentontheedge.be5.server.authentication.RoleService;
import com.google.common.base.Strings;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@Singleton
public class McpAuthenticationService
{
    private static final Logger log = Logger.getLogger(McpAuthenticationService.class.getName());

    private static final String BASIC = "Basic ";
    private static final String BEARER = "Bearer ";

    private final LoginService loginService;
    private final RoleService roleService;
    private final OAuthTokenStore tokenStore;

    @Inject
    public McpAuthenticationService(LoginService loginService, RoleService roleService, OAuthTokenStore tokenStore)
    {
        this.loginService = loginService;
        this.roleService = roleService;
        this.tokenStore = tokenStore;
    }

    /**
     * Checks the credentials of a Basic Authorization header.
     */
    public boolean authenticate(String authHeader)
    {
        String[] credentials = parseBasic(authHeader);
        return credentials != null && loginService.loginCheck(credentials[0], credentials[1].toCharArray());
    }

    /**
     * The user an Authorization header stands for - an OAuth bearer token or Basic credentials -
     * or null when it is missing, invalid, or the user may not use MCP.
     */
    public UserInfo authenticateUser(String authHeader, String remoteAddr)
    {
        if (authHeader != null && authHeader.startsWith(BEARER))
        {
            OAuthTokenStore.AccessToken token = tokenStore.validate(authHeader.substring(BEARER.length()).trim());
            return token != null ? token.user : null;
        }

        String[] credentials = parseBasic(authHeader);
        return credentials != null ? login(credentials[0], credentials[1], remoteAddr) : null;
    }

    /**
     * The user for a name and password, or null when they are wrong or the user may not use MCP.
     */
    public UserInfo login(String userName, String password, String remoteAddr)
    {
        if (Strings.isNullOrEmpty(userName) || password == null) return null;
        if (!loginService.loginCheck(userName, password.toCharArray()))
        {
            log.warning("MCP login refused user=" + userName + ": name or password not accepted by "
                    + loginService.getClass().getName());
            return null;
        }
        return createUserInfo(userName, remoteAddr);
    }

    /**
     * The user with its current roles, or null when the user has no roles or may not use MCP.
     */
    public UserInfo createUserInfo(String userName, String remoteAddr)
    {
        List<String> availableRoles = roleService.getAvailableRoles(userName);
        if (availableRoles == null || availableRoles.isEmpty())
        {
            log.warning("MCP access refused user=" + userName + ": no roles from " + roleService.getClass().getName());
            return null;
        }
        List<String> currentRoles = roleService.getCurrentRoles(userName);
        if (currentRoles == null || currentRoles.isEmpty()) currentRoles = availableRoles;

        UserInfo user = new UserInfo(userName, availableRoles, currentRoles);
        user.setRemoteAddr(remoteAddr);
        if (!isPermitted(user))
        {
            log.warning("MCP access refused user=" + userName + ": roles " + availableRoles + " are not among "
                    + McpOAuthConfig.ROLES_PROPERTY + "=" + McpOAuthConfig.allowedRoles());
            return null;
        }
        return user;
    }

    /**
     * Whether a logged in (not guest) user may use MCP, see {@link McpOAuthConfig#ROLES_PROPERTY}.
     */
    public boolean isPermitted(UserInfo user)
    {
        if (user == null || RoleType.ROLE_GUEST.equals(user.getUserName())) return false;
        List<String> allowedRoles = McpOAuthConfig.allowedRoles();
        return allowedRoles.isEmpty() || !Collections.disjoint(allowedRoles, user.getAvailableRoles());
    }

    private static String[] parseBasic(String authHeader)
    {
        if (Strings.isNullOrEmpty(authHeader) || !authHeader.startsWith(BASIC))
        {
            return null;
        }

        String decoded;
        try
        {
            decoded = new String(Base64.getDecoder().decode(authHeader.substring(BASIC.length()).trim()),
                    StandardCharsets.UTF_8);
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }

        int colonIndex = decoded.indexOf(':');
        if (colonIndex < 0)
        {
            return null;
        }
        return new String[]{decoded.substring(0, colonIndex), decoded.substring(colonIndex + 1)};
    }
}
