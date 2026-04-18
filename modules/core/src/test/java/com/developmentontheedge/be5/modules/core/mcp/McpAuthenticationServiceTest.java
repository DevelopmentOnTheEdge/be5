package com.developmentontheedge.be5.modules.core.mcp;

import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDBTest;
import com.developmentontheedge.be5.modules.core.services.LoginService;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Base64;

import static org.junit.Assert.*;

public class McpAuthenticationServiceTest extends CoreBe5ProjectDBTest
{
    @Inject
    private LoginService loginService;

    private McpAuthenticationService authService;

    private final String userName = "McpAuthTestUser";
    private final String password = "McpAuthTestPass";

    @Before
    public void init()
    {
        authService = new McpAuthenticationService(loginService);

        if (database.getEntity("users").count(ImmutableMap.of("user_name", userName)) == 0)
        {
            database.getEntity("users").add(ImmutableMap.of(
                    "user_name", userName,
                    "user_pass", password
            ));
        }
    }

    @Test
    public void testAuthenticateValidCredentials()
    {
        String authHeader = createBasicAuthHeader(userName, password);
        assertTrue(authService.authenticate(authHeader));
    }

    @Test
    public void testAuthenticateInvalidPassword()
    {
        String authHeader = createBasicAuthHeader(userName, "wrong_password");
        assertFalse(authService.authenticate(authHeader));
    }

    @Test
    public void testAuthenticateInvalidUser()
    {
        String authHeader = createBasicAuthHeader("nonexistent_user", password);
        assertFalse(authService.authenticate(authHeader));
    }

    @Test
    public void testAuthenticateNullHeader()
    {
        assertFalse(authService.authenticate(null));
    }

    @Test
    public void testAuthenticateEmptyHeader()
    {
        assertFalse(authService.authenticate(""));
    }

    @Test
    public void testAuthenticateNoBasicPrefix()
    {
        String encoded = Base64.getEncoder().encodeToString((userName + ":" + password).getBytes());
        assertFalse(authService.authenticate(encoded));
    }

    @Test
    public void testAuthenticateInvalidBase64()
    {
        assertFalse(authService.authenticate("Basic not_valid_base64!!!"));
    }

    @Test
    public void testAuthenticateNoColon()
    {
        String encoded = Base64.getEncoder().encodeToString((userName + password).getBytes());
        assertFalse(authService.authenticate("Basic " + encoded));
    }

    private String createBasicAuthHeader(String username, String password)
    {
        String encoded = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        return "Basic " + encoded;
    }
}