package com.developmentontheedge.be5.server.services.rememberme;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class AbstractRememberMeServiceTest
{
    @Test(expected = InvalidCookieException.class)
    public void nonBase64CookieShouldBeDetected() {
        new MockRememberMeServices().decodeCookie("nonBase64CookieValue%");
    }

    @Test
    public void setAndGetAreConsistent() throws Exception {
        MockRememberMeServices services = new MockRememberMeServices();
        assertNotNull(services.getCookieName());
        services.setCookieName("kookie");
        assertEquals(services.getCookieName(), "kookie");
        services.setTokenValiditySeconds(600);
        assertEquals(services.getTokenValiditySeconds(), 600);
    }

    @Test
    public void cookieShouldBeCorrectlyEncodedAndDecoded() throws Exception {
        MockRememberMeServices services = new MockRememberMeServices();
        String[] cookie = new String[] { "name:with:colon", "cookie", "tokens", "blah" };
        //MockRememberMeServices services = new MockRememberMeServices(uds);

        String encoded = services.encodeCookie(cookie);
        // '=' aren't allowed in version 0 cookies.
        assertFalse(encoded.endsWith("="));
        String[] decoded = services.decodeCookie(encoded);

        assertArrayEquals(decoded, new String[]{"name:with:colon", "cookie", "tokens", "blah"});
        //assertThat(decoded).containsExactly("name:with:colon", "cookie", "tokens", "blah");
    }

    @Test
    public void cookieWithOpenIDidentifierAsNameIsEncodedAndDecoded() throws Exception {
        String[] cookie = new String[] { "http://id.openid.zz", "cookie", "tokens",
                "blah" };
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

//    @Test
//    public void autoLoginShouldReturnNullIfNoLoginCookieIsPresented() {
//        MockRememberMeServices services = new MockRememberMeServices();
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        MockHttpServletResponse response = new MockHttpServletResponse();
//
//        assertThat(services.autoLogin(request, response)).isNull();
//
//        // shouldn't try to invalidate our cookie
//        assertThat(response.getCookie(
//                AbstractRememberMeService.REMEMBER_ME_KEY)).isNull();
//
//        request = new MockHttpServletRequest();
//        response = new MockHttpServletResponse();
//        // set non-login cookie
//        request.setCookies(new Cookie("mycookie", "cookie"));
//        assertThat(services.autoLogin(request, response)).isNull();
//        assertThat(response.getCookie(
//                AbstractRememberMeService.REMEMBER_ME_KEY)).isNull();
//    }
//
//    @Test
//    public void successfulAutoLoginReturnsExpectedAuthentication() throws Exception {
//        MockRememberMeServices services = new MockRememberMeServices(uds);
//        services.afterPropertiesSet();
//        assertThat(services.getUserDetailsService()).isNotNull();
//
//        MockHttpServletRequest request = new MockHttpServletRequest();
//
//        request.setCookies(createLoginCookie("cookie:1:2"));
//        MockHttpServletResponse response = new MockHttpServletResponse();
//
//        Authentication result = services.autoLogin(request, response);
//
//        assertThat(result).isNotNull();
//    }
//
//    @Test
//    public void autoLoginShouldFailIfCookieIsNotBase64() throws Exception {
//        MockRememberMeServices services = new MockRememberMeServices(uds);
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        MockHttpServletResponse response = new MockHttpServletResponse();
//
//        request.setCookies(new Cookie(
//                AbstractRememberMeService.REMEMBER_ME_KEY,
//                "ZZZ"));
//        Authentication result = services.autoLogin(request, response);
//        assertThat(result).isNull();
//        assertCookieCancelled(response);
//    }
//
//    @Test
//    public void autoLoginShouldFailIfCookieIsEmpty() throws Exception {
//        MockRememberMeServices services = new MockRememberMeServices(uds);
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        MockHttpServletResponse response = new MockHttpServletResponse();
//
//        request.setCookies(new Cookie(
//                AbstractRememberMeService.REMEMBER_ME_KEY, ""));
//        Authentication result = services.autoLogin(request, response);
//        assertThat(result).isNull();
//        assertCookieCancelled(response);
//    }
//
//    @Test
//    public void autoLoginShouldFailIfInvalidCookieExceptionIsRaised() {
//        MockRememberMeServices services = new MockRememberMeServices(
//                new MockUserDetailsService(joe, true));
//
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        // Wrong number of tokens
//        request.setCookies(createLoginCookie("cookie:1"));
//        MockHttpServletResponse response = new MockHttpServletResponse();
//
//        Authentication result = services.autoLogin(request, response);
//
//        assertThat(result).isNull();
//
//        assertCookieCancelled(response);
//    }
//
//    @Test
//    public void autoLoginShouldFailIfUserNotFound() {
//        uds.setThrowException(true);
//        MockRememberMeServices services = new MockRememberMeServices(uds);
//
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        request.setCookies(createLoginCookie("cookie:1:2"));
//        MockHttpServletResponse response = new MockHttpServletResponse();
//
//        Authentication result = services.autoLogin(request, response);
//
//        assertThat(result).isNull();
//
//        assertCookieCancelled(response);
//    }
//
//    @Test
//    public void autoLoginShouldFailIfUserAccountIsLocked() {
//        MockRememberMeServices services = new MockRememberMeServices(uds);
//        services.setUserDetailsChecker(new AccountStatusUserDetailsChecker());
//        uds.toReturn = new User("joe", "password", false, true, true, true,
//                joe.getAuthorities());
//
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        request.setCookies(createLoginCookie("cookie:1:2"));
//        MockHttpServletResponse response = new MockHttpServletResponse();
//
//        Authentication result = services.autoLogin(request, response);
//
//        assertThat(result).isNull();
//
//        assertCookieCancelled(response);
//    }
//
//    @Test
//    public void loginFailShouldCancelCookie() {
//        uds.setThrowException(true);
//        MockRememberMeServices services = new MockRememberMeServices(uds);
//
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        request.setContextPath("contextpath");
//        request.setCookies(createLoginCookie("cookie:1:2"));
//        MockHttpServletResponse response = new MockHttpServletResponse();
//
//        services.loginFail(request, response);
//
//        assertCookieCancelled(response);
//    }
//
//    @Test
//    public void logoutShouldCancelCookie() throws Exception {
//        MockRememberMeServices services = new MockRememberMeServices(uds);
//        services.setCookieDomain("spring.io");
//
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        request.setContextPath("contextpath");
//        request.setCookies(createLoginCookie("cookie:1:2"));
//        MockHttpServletResponse response = new MockHttpServletResponse();
//
//        services.logout(request, response, Mockito.mock(Authentication.class));
//        // Try again with null Authentication
//        response = new MockHttpServletResponse();
//
//        services.logout(request, response, null);
//
//        assertCookieCancelled(response);
//
//        Cookie returnedCookie = response.getCookie(
//                AbstractRememberMeService.REMEMBER_ME_KEY);
//        assertThat(returnedCookie.getDomain(), "spring.io");
//    }
//
//    @Test(expected = CookieTheftException.class)
//    public void cookieTheftExceptionShouldBeRethrown() {
//        MockRememberMeServices services = new MockRememberMeServices(uds) {
//
//            protected UserDetails processAutoLoginCookie(String[] cookieTokens,
//                                                         HttpServletRequest request, HttpServletResponse response) {
//                throw new CookieTheftException("Pretending cookie was stolen");
//            }
//        };
//
//        MockHttpServletRequest request = new MockHttpServletRequest();
//
//        request.setCookies(createLoginCookie("cookie:1:2"));
//        MockHttpServletResponse response = new MockHttpServletResponse();
//
//        services.autoLogin(request, response);
//    }
//
//    @Test
//    public void loginSuccessCallsOnLoginSuccessCorrectly() {
//        MockRememberMeServices services = new MockRememberMeServices(uds);
//
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        MockHttpServletResponse response = new MockHttpServletResponse();
//        Authentication auth = new UsernamePasswordAuthenticationToken("joe", "password");
//
//        // No parameter set
//        services.loginSuccess(request, response, auth);
//        assertThat(services.loginSuccessCalled).isFalse();
//
//        // Parameter set to true
//        services = new MockRememberMeServices(uds);
//        request.setParameter(MockRememberMeServices.DEFAULT_PARAMETER, "true");
//        services.loginSuccess(request, response, auth);
//        assertThat(services.loginSuccessCalled).isTrue();
//
//        // Different parameter name, set to true
//        services = new MockRememberMeServices(uds);
//        services.setParameter("my_parameter");
//        request.setParameter("my_parameter", "true");
//        services.loginSuccess(request, response, auth);
//        assertThat(services.loginSuccessCalled).isTrue();
//
//        // Parameter set to false
//        services = new MockRememberMeServices(uds);
//        request.setParameter(MockRememberMeServices.DEFAULT_PARAMETER, "false");
//        services.loginSuccess(request, response, auth);
//        assertThat(services.loginSuccessCalled).isFalse();
//
//        // alwaysRemember set to true
//        services = new MockRememberMeServices(uds);
//        services.setAlwaysRemember(true);
//        services.loginSuccess(request, response, auth);
//        assertThat(services.loginSuccessCalled).isTrue();
//    }
//
//    @Test
//    public void setCookieUsesCorrectNamePathAndValue() {
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        MockHttpServletResponse response = new MockHttpServletResponse();
//        request.setContextPath("contextpath");
//        MockRememberMeServices services = new MockRememberMeServices(uds) {
//
//            protected String encodeCookie(String[] cookieTokens) {
//                return cookieTokens[0];
//            }
//        };
//        services.setCookieName("mycookiename");
//        services.setCookie(new String[] { "mycookie" }, 1000, request, response);
//        Cookie cookie = response.getCookie("mycookiename");
//
//        assertThat(cookie).isNotNull();
//        assertThat(cookie.getValue(), "mycookie");
//        assertThat(cookie.getName(), "mycookiename");
//        assertThat(cookie.getPath(), "contextpath");
//        assertThat(cookie.getSecure()).isFalse();
//    }
//
//    @Test
//    public void setCookieSetsSecureFlagIfConfigured() throws Exception {
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        MockHttpServletResponse response = new MockHttpServletResponse();
//        request.setContextPath("contextpath");
//
//        MockRememberMeServices services = new MockRememberMeServices(uds) {
//
//            protected String encodeCookie(String[] cookieTokens) {
//                return cookieTokens[0];
//            }
//        };
//        services.setUseSecureCookie(true);
//        services.setCookie(new String[] { "mycookie" }, 1000, request, response);
//        Cookie cookie = response.getCookie(
//                AbstractRememberMeService.REMEMBER_ME_KEY);
//        assertThat(cookie.getSecure()).isTrue();
//    }
//
//    @Test
//    public void setCookieSetsIsHttpOnlyFlagByDefault() throws Exception {
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        MockHttpServletResponse response = new MockHttpServletResponse();
//        request.setContextPath("contextpath");
//
//        MockRememberMeServices services = new MockRememberMeServices(uds);
//        services.setCookie(new String[] { "mycookie" }, 1000, request, response);
//        Cookie cookie = response.getCookie(
//                AbstractRememberMeService.REMEMBER_ME_KEY);
//        assertThat(cookie.isHttpOnly()).isTrue();
//    }
//
//    // SEC-2791
//    @Test
//    public void setCookieMaxAge0VersionSet() {
//        MockRememberMeServices services = new MockRememberMeServices();
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        MockHttpServletResponse response = new MockHttpServletResponse();
//
//        services.setCookie(new String[] { "value" }, 0, request, response);
//
//        Cookie cookie = response.getCookie(
//                AbstractRememberMeService.REMEMBER_ME_KEY);
//        assertThat(cookie.getVersion(), 1);
//    }
//
//    // SEC-2791
//    @Test
//    public void setCookieMaxAgeNegativeVersionSet() {
//        MockRememberMeServices services = new MockRememberMeServices();
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        MockHttpServletResponse response = new MockHttpServletResponse();
//
//        services.setCookie(new String[] { "value" }, -1, request, response);
//
//        Cookie cookie = response.getCookie(
//                AbstractRememberMeService.REMEMBER_ME_KEY);
//        assertThat(cookie.getVersion(), 1);
//    }
//
//    // SEC-2791
//    @Test
//    public void setCookieMaxAge1VersionSet() {
//        MockRememberMeServices services = new MockRememberMeServices();
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        MockHttpServletResponse response = new MockHttpServletResponse();
//
//        services.setCookie(new String[] { "value" }, 1, request, response);
//
//        Cookie cookie = response.getCookie(
//                AbstractRememberMeService.REMEMBER_ME_KEY);
//        assertThat(cookie.getVersion()).isZero();
//    }
//
//    @Test
//    public void setCookieDomainValue() {
//        MockRememberMeServices services = new MockRememberMeServices();
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        MockHttpServletResponse response = new MockHttpServletResponse();
//
//        services.setCookieName("mycookiename");
//        services.setCookieDomain("spring.io");
//        services.setCookie(new String[] { "mycookie" }, 1000, request, response);
//        Cookie cookie = response.getCookie("mycookiename");
//
//        assertThat(cookie).isNotNull();
//        assertThat(cookie.getDomain(), "spring.io");
//    }
//
//    private Cookie[] createLoginCookie(String cookieToken) {
//        MockRememberMeServices services = new MockRememberMeServices(uds);
//        Cookie cookie = new Cookie(
//                AbstractRememberMeService.REMEMBER_ME_KEY,
//                services.encodeCookie(
//                        cookieToken.split(":")));
//
//        return new Cookie[] { cookie };
//    }
//
//    private void assertCookieCancelled(MockHttpServletResponse response) {
//        Cookie returnedCookie = response.getCookie(
//                AbstractRememberMeService.REMEMBER_ME_KEY);
//        assertThat(returnedCookie).isNotNull();
//        assertThat(returnedCookie.getMaxAge()).isZero();
//    }

    // ~ Inner Classes
    // ==================================================================================================

    static class MockRememberMeServices extends AbstractRememberMeService {

        boolean loginSuccessCalled;

        MockRememberMeServices() {
            super();
        }

        public void onLoginSuccess(HttpServletRequest request,
                                      HttpServletResponse response, String userName) {
            loginSuccessCalled = true;
        }

        protected String processAutoLoginCookie(String[] cookieTokens,
                                                     HttpServletRequest request, HttpServletResponse response)
                throws RememberMeAuthenticationException {
            if (cookieTokens.length != 3) {
                throw new InvalidCookieException("deliberate exception");
            }

            return "joe";
        }
    }
}
