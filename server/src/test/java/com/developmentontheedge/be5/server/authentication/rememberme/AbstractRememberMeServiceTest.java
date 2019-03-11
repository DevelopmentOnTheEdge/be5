package com.developmentontheedge.be5.server.authentication.rememberme;

import com.developmentontheedge.be5.test.ServerTestResponse;
import com.developmentontheedge.be5.test.mocks.ServerTestRequest;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import org.junit.Test;

import javax.servlet.http.Cookie;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AbstractRememberMeServiceTest
{
    @Test(expected = InvalidCookieException.class)
    public void nonBase64CookieShouldBeDetected()
    {
        new MockRememberMeServices().decodeCookie("nonBase64CookieValue%");
    }

    @Test
    public void setAndGetAreConsistent() throws Exception
    {
        MockRememberMeServices services = new MockRememberMeServices();
        assertNotNull(services.getCookieName());
        services.setCookieName("kookie");
        assertEquals(services.getCookieName(), "kookie");
        services.setTokenValiditySeconds(600);
        assertEquals(services.getTokenValiditySeconds(), 600);
    }

    @Test
    public void cookieShouldBeCorrectlyEncodedAndDecoded() throws Exception
    {
        MockRememberMeServices services = new MockRememberMeServices();
        String[] cookie = new String[]{"name:with:colon", "cookie", "tokens", "blah"};
        //MockRememberMeServices services = new MockRememberMeServices(uds);

        String encoded = services.encodeCookie(cookie);
        // '=' aren't allowed in version 0 cookies.
        assertFalse(encoded.endsWith("="));
        String[] decoded = services.decodeCookie(encoded);

        assertArrayEquals(decoded, new String[]{"name:with:colon", "cookie", "tokens", "blah"});
        //assertThat(decoded).containsExactly("name:with:colon", "cookie", "tokens", "blah");
    }

    @Test
    public void cookieWithOpenIDidentifierAsNameIsEncodedAndDecoded() throws Exception
    {
        String[] cookie = new String[]{"http://id.openid.zz", "cookie", "tokens",
                "blah"};
        MockRememberMeServices services = new MockRememberMeServices();

        String[] decoded = services.decodeCookie(services.encodeCookie(cookie));
        assertEquals(4, decoded.length);
        assertEquals(decoded[0], "http://id.openid.zz");

        // Check https (SEC-1410)
        cookie[0] = "https://id.openid.zz";
        decoded = services.decodeCookie(services.encodeCookie(cookie));
        assertEquals(4, decoded.length);
        assertEquals(decoded[0], "https://id.openid.zz");
    }

    @Test
    public void autoLoginShouldReturnNullIfNoLoginCookieIsPresented() {
        MockRememberMeServices services = new MockRememberMeServices();
        ServerTestRequest request = new ServerTestRequest();
        ServerTestResponse response = new ServerTestResponse();

        assertNull(services.autoLogin(request, response));

        // shouldn't try to invalidate our cookie
        assertNull(response.getCookie(
                AbstractRememberMeService.REMEMBER_ME_KEY));

        request = new ServerTestRequest();
        response = new ServerTestResponse();
        // set non-login cookie
        request.setCookies(new Cookie("mycookie", "cookie"));
        assertNull(services.autoLogin(request, response));
        assertNull(response.getCookie(
                AbstractRememberMeService.REMEMBER_ME_KEY));
    }

    @Test
    public void successfulAutoLoginReturnsExpectedAuthentication() throws Exception {
        MockRememberMeServices services = new MockRememberMeServices();

        ServerTestRequest request = new ServerTestRequest();

        request.setCookies(createLoginCookie("cookie:1:2"));
        ServerTestResponse response = new ServerTestResponse();

        String result = services.autoLogin(request, response);

        assertNotNull(result);
    }

    @Test
    public void autoLoginShouldFailIfCookieIsNotBase64() throws Exception {
        MockRememberMeServices services = new MockRememberMeServices();
        ServerTestRequest request = new ServerTestRequest();
        ServerTestResponse response = new ServerTestResponse();

        request.setCookies(new Cookie(
                AbstractRememberMeService.REMEMBER_ME_KEY,
                "ZZZ"));
        String result = services.autoLogin(request, response);
        assertNull(result);
        assertCookieCancelled(response);
    }

    @Test
    public void autoLoginShouldFailIfCookieIsEmpty() throws Exception {
        MockRememberMeServices services = new MockRememberMeServices();
        ServerTestRequest request = new ServerTestRequest();
        ServerTestResponse response = new ServerTestResponse();

        request.setCookies(new Cookie(
                AbstractRememberMeService.REMEMBER_ME_KEY, ""));
        String result = services.autoLogin(request, response);
        assertNull(result);
        assertCookieCancelled(response);
    }

    @Test
    public void autoLoginShouldFailIfInvalidCookieExceptionIsRaised() {
        MockRememberMeServices services = new MockRememberMeServices();

        ServerTestRequest request = new ServerTestRequest();
        // Wrong number of tokens
        request.setCookies(createLoginCookie("cookie:1"));
        ServerTestResponse response = new ServerTestResponse();

        String result = services.autoLogin(request, response);

        assertNull(result);

        assertCookieCancelled(response);
    }

    @Test
    public void logoutShouldCancelCookie() throws Exception {
        MockRememberMeServices services = new MockRememberMeServices();

        ServerTestRequest request = new ServerTestRequest();
        request.setCookies(createLoginCookie("cookie:1:2"));
        ServerTestResponse response = new ServerTestResponse();

        services.logout(request, response, "test");
        // Try again with null Authentication
        response = new ServerTestResponse();

        services.logout(request, response, null);

        assertCookieCancelled(response);
    }

    @Test(expected = CookieTheftException.class)
    public void cookieTheftExceptionShouldBeRethrown() {
        MockRememberMeServices services = new MockRememberMeServices() {

            protected String processAutoLoginCookie(String[] cookieTokens, Request request, Response response) {
                throw new CookieTheftException("Pretending cookie was stolen");
            }
        };

        ServerTestRequest request = new ServerTestRequest();

        request.setCookies(createLoginCookie("cookie:1:2"));
        ServerTestResponse response = new ServerTestResponse();

        services.autoLogin(request, response);
    }

    @Test
    public void setCookieUsesCorrectNamePathAndValue() {
        ServerTestRequest request = new ServerTestRequest();
        ServerTestResponse response = new ServerTestResponse();
        MockRememberMeServices services = new MockRememberMeServices() {

            protected String encodeCookie(String[] cookieTokens) {
                return cookieTokens[0];
            }
        };
        services.setCookieName("mycookiename");
        services.setCookie(new String[] { "mycookie" }, 1000, request, response);
        Cookie cookie = response.getCookie("mycookiename");

        assertNotNull(cookie);
        assertEquals("mycookie", cookie.getValue());
        assertEquals("mycookiename", cookie.getName());
        assertEquals("/", cookie.getPath());
        assertFalse(cookie.getSecure());
    }

    @Test
    public void setCookieSetsIsHttpOnlyFlagByDefault() throws Exception {
        ServerTestRequest request = new ServerTestRequest();
        ServerTestResponse response = new ServerTestResponse();

        MockRememberMeServices services = new MockRememberMeServices();
        services.setCookie(new String[] { "mycookie" }, 1000, request, response);
        Cookie cookie = response.getCookie(
                AbstractRememberMeService.REMEMBER_ME_KEY);
        assertTrue(cookie.isHttpOnly());
    }

    // SEC-2791
    @Test
    public void setCookieMaxAge0VersionSet() {
        MockRememberMeServices services = new MockRememberMeServices();
        ServerTestRequest request = new ServerTestRequest();
        ServerTestResponse response = new ServerTestResponse();

        services.setCookie(new String[] { "value" }, 0, request, response);

        Cookie cookie = response.getCookie(
                AbstractRememberMeService.REMEMBER_ME_KEY);
        assertEquals(1, cookie.getVersion());
    }

    // SEC-2791
    @Test
    public void setCookieMaxAgeNegativeVersionSet() {
        MockRememberMeServices services = new MockRememberMeServices();
        ServerTestRequest request = new ServerTestRequest();
        ServerTestResponse response = new ServerTestResponse();

        services.setCookie(new String[] { "value" }, -1, request, response);

        Cookie cookie = response.getCookie(
                AbstractRememberMeService.REMEMBER_ME_KEY);
        assertEquals(1, cookie.getVersion());
    }

    // SEC-2791
    @Test
    public void setCookieMaxAge1VersionSet() {
        MockRememberMeServices services = new MockRememberMeServices();
        ServerTestRequest request = new ServerTestRequest();
        ServerTestResponse response = new ServerTestResponse();

        services.setCookie(new String[] { "value" }, 1, request, response);

        Cookie cookie = response.getCookie(
                AbstractRememberMeService.REMEMBER_ME_KEY);
        assertEquals(0, cookie.getVersion());
    }

    private Cookie[] createLoginCookie(String cookieToken) {
        MockRememberMeServices services = new MockRememberMeServices();
        Cookie cookie = new Cookie(
                AbstractRememberMeService.REMEMBER_ME_KEY,
                services.encodeCookie(
                        cookieToken.split(":")));

        return new Cookie[] { cookie };
    }

    private void assertCookieCancelled(ServerTestResponse response) {
        Cookie returnedCookie = response.getCookie(
                AbstractRememberMeService.REMEMBER_ME_KEY);
        assertNotNull(returnedCookie);
        assertEquals(0, returnedCookie.getMaxAge());
    }

    // ~ Inner Classes
    // ==================================================================================================

    static class MockRememberMeServices extends AbstractRememberMeService
    {
        boolean loginSuccessCalled;

        MockRememberMeServices()
        {
            super();
        }

        public void onLoginSuccess(Request request, Response response, String userName)
        {
            loginSuccessCalled = true;
        }

        protected String processAutoLoginCookie(String[] cookieTokens, Request request, Response response)
                throws RememberMeAuthenticationException
        {
            if (cookieTokens.length != 3)
            {
                throw new InvalidCookieException("deliberate exception");
            }

            return "joe";
        }
    }
}
