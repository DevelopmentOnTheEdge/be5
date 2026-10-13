package com.developmentontheedge.be5.modules.core.mcp;

import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDBTest;
import com.developmentontheedge.be5.security.UserInfo;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Base64;

import static org.junit.Assert.*;

public class McpAuthenticationServiceTest extends CoreBe5ProjectDBTest
{
    @Inject
    private McpAuthenticationService authService;

    private final String userName = "McpAuthTestUser";
    private final String password = "McpAuthTestPass";

    @Before
    public void init()
    {
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
    public void testAuthenticateValidCredentialsAlt()
    {
        String authHeader = "Basic YW5uYUBkZXZlbG9wbWVudG9udGhlZWRnZS5jb206OnRva2VuOjMyNWE5YWQ3LTI1YTYtNDYxOC1hNzAyLWI2MWM4MDIyMDJjNw==";
        if (database.getEntity("users").count(ImmutableMap.of("user_name", "anna@developmentontheedge.com")) == 0)
        {
            database.getEntity("users").add(ImmutableMap.of(
                    "user_name", "anna@developmentontheedge.com",
                    "user_pass", ":token:325a9ad7-25a6-4618-a702-b61c802202c7"
            ));
        }
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

    @Test
    public void testAuthenticateUserNeedsRole()
    {
        String authHeader = createBasicAuthHeader(userName, password);
        database.getEntity("user_roles").removeBy(ImmutableMap.of("user_name", userName));
        assertNull(authService.authenticateUser(authHeader, "127.0.0.1"));

        database.getEntity("user_roles").add(ImmutableMap.of("user_name", userName, "role_name", "Administrator"));
        UserInfo user = authService.authenticateUser(authHeader, "127.0.0.1");
        assertEquals(userName, user.getUserName());
        assertEquals("127.0.0.1", user.getRemoteAddr());

        assertNull(authService.authenticateUser(createBasicAuthHeader(userName, "wrong_password"), "127.0.0.1"));
        assertNull(authService.authenticateUser("Bearer mcp_at_unknown", "127.0.0.1"));
    }

    private String createBasicAuthHeader(String username, String password)
    {
        String encoded = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        return "Basic " + encoded;
    }
}