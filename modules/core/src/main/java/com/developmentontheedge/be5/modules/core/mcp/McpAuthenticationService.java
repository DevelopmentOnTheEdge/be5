package com.developmentontheedge.be5.modules.core.mcp;

import com.developmentontheedge.be5.modules.core.services.LoginService;
import com.google.common.base.Strings;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Base64;

@Singleton
public class McpAuthenticationService
{
    private final LoginService loginService;

    @Inject
    public McpAuthenticationService(LoginService loginService)
    {
        this.loginService = loginService;
    }

    public boolean authenticate(String authHeader)
    {
        if (Strings.isNullOrEmpty(authHeader) || !authHeader.startsWith("Basic "))
        {
            return false;
        }

        String encoded = authHeader.substring(6);
        String decoded;
        try
        {
            decoded = new String(Base64.getDecoder().decode(encoded));
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }

        int colonIndex = decoded.indexOf(':');
        if (colonIndex < 0)
        {
            return false;
        }

        String username = decoded.substring(0, colonIndex);
        String password = decoded.substring(colonIndex + 1);

        return loginService.loginCheck(username, password.toCharArray());
    }
}