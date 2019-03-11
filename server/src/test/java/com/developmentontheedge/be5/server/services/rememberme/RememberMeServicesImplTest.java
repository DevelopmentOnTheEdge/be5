package com.developmentontheedge.be5.server.services.rememberme;

import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import com.developmentontheedge.be5.util.DateUtils;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;

public class RememberMeServicesImplTest extends ServerBe5ProjectTest
{
    private PersistentRememberMeServices services;

    private MockTokenRepository repo;
    private HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    private HttpServletResponse mockResponce = mock(HttpServletResponse.class);

    @Before
    public void setUpData() throws Exception {
        initGuest();
        services = new PersistentRememberMeServices(new InMemoryTokenRepositoryImpl(), userAwareMeta);
        services.setCookieName("mycookiename");
        // Default to 100 days (see SEC-1081).
        services.setTokenValiditySeconds(100 * 24 * 60 * 60);
    }

    @Test(expected = InvalidCookieException.class)
    public void loginIsRejectedWithWrongNumberOfCookieTokens() {
        services.processAutoLoginCookie(new String[] { "series", "token", "extra" },
                mockRequest, mockResponce);
    }

    @Test(expected = RememberMeAuthenticationException.class)
    public void loginIsRejectedWhenNoTokenMatchingSeriesIsFound() {
        services = create(null);
        services.processAutoLoginCookie(new String[] { "series", "token" },
                mockRequest, mockResponce);
    }

    @Test(expected = RememberMeAuthenticationException.class)
    public void loginIsRejectedWhenTokenIsExpired() {
        services = create(new PersistentRememberMeToken("joe", "series", "token",
                new Timestamp(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(1) - 100)));
        services.setTokenValiditySeconds(1);

        services.processAutoLoginCookie(new String[] { "series", "token" },
                mockRequest, mockResponce);
    }

    @Test(expected = CookieTheftException.class)
    public void cookieTheftIsDetectedWhenSeriesAndTokenDontMatch() {
        services = create(new PersistentRememberMeToken("joe", "series", "wrongtoken",
                DateUtils.currentTimestamp()));
        services.processAutoLoginCookie(new String[] { "series", "token" },
                mockRequest, mockResponce);
    }

//    @Test
//    public void successfulAutoLoginCreatesNewTokenAndCookieWithSameSeries() {
//        services = create(new PersistentRememberMeToken("joe", "series", "token",
//                DateUtils.currentTimestamp()));
//        // 12 => b64 length will be 16
//        services.setTokenLength(12);
//        HttpServletResponse response = mock(HttpServletResponse.class);
//        services.processAutoLoginCookie(new String[] { "series", "token" },
//                mockRequest, response);
//        assertEquals(repo.getStoredToken().getSeries(), "series");
//        assertEquals(repo.getStoredToken().getTokenValue().length(), 16);
//        String[] cookie = services.decodeCookie(response.getCookie("mycookiename")
//                .getValue());
//        assertEquals(cookie[0], "series");
//        assertEquals(cookie[1], repo.getStoredToken().getTokenValue());
//    }

//    @Test
//    public void loginSuccessCreatesNewTokenAndCookieWithNewSeries() {
//        services = create(null);
//        services.setTokenLength(12);
//        services.setSeriesLength(12);
//        HttpServletResponse response = mock(HttpServletResponse.class);
//        services.onLoginSuccess(mockRequest, response, "joe");
//        assertEquals(repo.getStoredToken().getSeries().length(), 16);
//        assertEquals(repo.getStoredToken().getTokenValue().length(), 16);
//
//        String[] cookie = services.decodeCookie(response.getCookie("mycookiename").getValue());
//
//        assertEquals(cookie[0], repo.getStoredToken().getSeries());
//        assertEquals(cookie[1], repo.getStoredToken().getTokenValue());
//    }
//
//    @Test
//    public void logoutClearsUsersTokenAndCookie() throws Exception {
//        Cookie cookie = new Cookie("mycookiename", "somevalue");
//        HttpServletRequest request = mock(HttpServletRequest.class);
//        request.setCookies(cookie);
//        HttpServletResponse response = mock(HttpServletResponse.class);
//        services = create(new PersistentRememberMeToken("joe", "series", "token",
//                DateUtils.currentTimestamp()));
//        services.logout(request, response, "joe");
//        Cookie returnedCookie = response.getCookie("mycookiename");
//        assertNotNull(returnedCookie);
//        assertEquals(0, returnedCookie.getMaxAge());
//
//        // SEC-1280
//        services.logout(request, response, null);
//    }

    private PersistentRememberMeServices create(PersistentRememberMeToken token) {
        repo = new MockTokenRepository(token);
        PersistentRememberMeServices services = new PersistentRememberMeServices(repo, userAwareMeta);

        services.setCookieName("mycookiename");
        return services;
    }

    public class InMemoryTokenRepositoryImpl implements PersistentTokenRepository {
        private final Map<String, PersistentRememberMeToken> seriesTokens = new HashMap<>();

        public synchronized void createNewToken(PersistentRememberMeToken token) {
            PersistentRememberMeToken current = seriesTokens.get(token.getSeries());

            if (current != null) {
                throw new RuntimeException("Series Id '" + token.getSeries()
                        + "' already exists!");
            }

            seriesTokens.put(token.getSeries(), token);
        }

        public synchronized void updateToken(String series, String tokenValue, Timestamp lastUsed) {
            PersistentRememberMeToken token = getTokenForSeries(series);

            PersistentRememberMeToken newToken = new PersistentRememberMeToken(
                    token.getUsername(), series, tokenValue, lastUsed);

            // Store it, overwriting the existing one.
            seriesTokens.put(series, newToken);
        }

        public synchronized PersistentRememberMeToken getTokenForSeries(String seriesId) {
            return seriesTokens.get(seriesId);
        }

        public synchronized void removeUserTokens(String username) {
            Iterator<String> series = seriesTokens.keySet().iterator();

            while (series.hasNext()) {
                String seriesId = series.next();

                PersistentRememberMeToken token = seriesTokens.get(seriesId);

                if (username.equals(token.getUsername())) {
                    series.remove();
                }
            }
        }
    }

    private class MockTokenRepository implements PersistentTokenRepository
    {
        private PersistentRememberMeToken storedToken;

        private MockTokenRepository(PersistentRememberMeToken token)
        {
            storedToken = token;
        }

        public void createNewToken(PersistentRememberMeToken token)
        {
            storedToken = token;
        }

        public void updateToken(String series, String tokenValue, Timestamp lastUsed)
        {
            storedToken = new PersistentRememberMeToken(storedToken.getUsername(),
                    storedToken.getSeries(), tokenValue, lastUsed);
        }

        public PersistentRememberMeToken getTokenForSeries(String seriesId)
        {
            return storedToken;
        }

        public void removeUserTokens(String username)
        {
        }

        PersistentRememberMeToken getStoredToken()
        {
            return storedToken;
        }
    }
}
