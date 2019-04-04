package com.developmentontheedge.be5.server.authentication.rememberme;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.meta.UserAwareMeta;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copy from @see <a href="https://github.com/jtalks-org/jcommune">https://github.com/jtalks-org/jcommune</a>
 * <p>
 * Implements our custom Remember Me service to replace the Spring default one. This implementation removes Remember Me
 * token only for a single session and prevents sequent remember me authentication from single client..
 * <p><b>Justification:</b> Spring's * {@link PersistentRememberMeServices} removes all the tokens from DB
 * for a user whose session expired - even the sessions started on a different machine or device. Thus users were
 * frustrated when their sessions expired on the machines where the Remember Me checkbox was checked.
 * </p>
 */
public class ThrottlingRememberMeService extends PersistentRememberMeServices
{
    private static final Logger log = Logger.getLogger(ThrottlingRememberMeService.class.getName());

    private final static String REMOVE_TOKEN_QUERY = "DELETE FROM persistent_logins WHERE series = ? AND token = ?";
    // We should store a lot of tokens to prevent cache overflow
    private static final int TOKEN_CACHE_MAX_SIZE = 100;
    //private final RememberMeCookieDecoder rememberMeCookieDecoder;
    private final DbService db;
    private final Map<String, CachedRememberMeTokenInfo> tokenCache = new ConcurrentHashMap<>();

    // 5 seconds should be enough for processing request and sending response to client
    private int cachedTokenValidityTime = 5 * 1000;

    /**
     * @param db needed to execute the sql queries
     */
    @Inject
    public ThrottlingRememberMeService(PersistentTokenRepository tokenRepository, UserAwareMeta userAwareMeta,
                                       DbService db)
    {
        super(tokenRepository, userAwareMeta);
        this.db = db;
    }

    /**
     * Causes a logout to be completed. The method must complete successfully.
     * Removes client's token which is extracted from the HTTP request.
     * {@inheritDoc}
     */
    @Override
    public void logout(Request request, Response response, String username)
    {
        String cookie = extractRememberMeCookie(request);
        if (cookie != null)
        {
            String[] seriesAndToken = decodeCookie(cookie);
            if (log.isLoggable(Level.FINE))
            {
                log.fine("Logout of user " + (username == null ? "Unknown" : username));
            }
            cancelCookie(request, response);
            db.update(REMOVE_TOKEN_QUERY, (Object[]) seriesAndToken);
            tokenCache.remove(seriesAndToken[0]);
            validateTokenCache();
        }
    }

    /**
     * Solution for preventing "remember-me" bug. Some browsers sends preloading requests to server to speed-up
     * page loading. It may cause error when response of preload request not returned to client and second request
     * from client was send. This method implementation stores token in cache for <link>CACHED_TOKEN_VALIDITY_TIME</link>
     * milliseconds and check token presence in cache before process authentication. If there is no equivalent token in
     * cache authentication performs normally. If equivalent present in cache we should not update token in database.
     * This approach can provide acceptable security level and prevent errors.
     * {@inheritDoc}
     *
     * @see <a href="http://jira.jtalks.org/browse/JC-1743">JC-1743</a>
     * @see <a href="https://developers.google.com/chrome/whitepapers/prerender?csw=1">Page preloading in Google Chrome</a>
     */
    @Override
    protected String processAutoLoginCookie(String[] cookieTokens, Request request, Response response)
    {
        if (cookieTokens.length != 2)
        {
            throw new InvalidCookieException("Cookie token did not contain " + 2
                    + " tokens, but contained '" + Arrays.asList(cookieTokens) + "'");
        }

        final String presentedSeries = cookieTokens[0];
        final String presentedToken = cookieTokens[1];

        PersistentRememberMeToken token = tokenRepository
                .getTokenForSeries(presentedSeries);

        if (token == null)
        {
            // No series match, so we can't authenticate using this cookie
            throw new RememberMeAuthenticationException("No persistent token found for series id: " + presentedSeries);
        }

        String details;

        if (isTokenCached(presentedSeries, presentedToken))
        {
            tokenCache.remove(presentedSeries);
            details = token.getUsername();
            rewriteCookie(token, request, response);
        }
        else
        {
            /* IMPORTANT: We should store token in cache before calling <code>loginWithSpringSecurity</code> method.
               Because execution of this method can take a long time.
             */
            cacheToken(token);
            try
            {
                details = loginWithSpringSecurity(cookieTokens, request, response);
                //We should remove token from cache if cookie really was stolen or other authentication error occurred
            }
            catch (RememberMeAuthenticationException ex)
            {
                tokenCache.remove(token.getSeries());
                throw ex;
            }
        }
        validateTokenCache();

        return details;
    }

    /**
     * Calls PersistentTokenBasedRememberMeServices#processAutoLoginCookie method.
     * Needed for possibility to test.
     */
    @VisibleForTesting
    String loginWithSpringSecurity(String[] cookieTokens, Request request, Response response)
    {
        return super.processAutoLoginCookie(cookieTokens, request, response);
    }

    /**
     * Sets valid cookie to response
     * Needed for possibility to test.
     */
    @VisibleForTesting
    void rewriteCookie(PersistentRememberMeToken token, Request request, Response response)
    {
        setCookie(new String[]{token.getSeries(), token.getTokenValue()}, getTokenValiditySeconds(), request, response);
    }

    /**
     * Stores token in cache.
     *
     * @param token Token to be stored
     * @see CachedRememberMeTokenInfo
     */
    private void cacheToken(PersistentRememberMeToken token)
    {
        if (tokenCache.size() >= TOKEN_CACHE_MAX_SIZE)
        {
            validateTokenCache();
        }
        CachedRememberMeTokenInfo tokenWrapper = new CachedRememberMeTokenInfo(token.getTokenValue(), System.currentTimeMillis());
        tokenCache.put(token.getSeries(), tokenWrapper);
    }

    /**
     * Removes from cache tokens which were stored more than <link>CACHED_TOKEN_VALIDITY_TIME</link> milliseconds ago.
     */
    private void validateTokenCache()
    {
        for (Map.Entry<String, CachedRememberMeTokenInfo> entry : tokenCache.entrySet())
        {
            if (!isTokenInfoValid(entry.getValue()))
            {
                tokenCache.remove(entry.getKey());
            }
        }
    }

    /**
     * Checks if given tokenInfo valid.
     *
     * @param tokenInfo Token wrapper to be checked
     * @return <code>true</code> tokenInfo was stored in cache less than <link>CACHED_TOKEN_VALIDITY_TIME</link> milliseconds ago.
     * <code>false</code> otherwise.
     * @see CachedRememberMeTokenInfo
     */
    private boolean isTokenInfoValid(CachedRememberMeTokenInfo tokenInfo)
    {
        if ((System.currentTimeMillis() - tokenInfo.getCachingTime()) >= cachedTokenValidityTime)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * Checks if token with given series and value stored in cache
     *
     * @param series series to be checked
     * @param value  value to be checked
     * @return <code>true</code> if token stored in cache< <code>false</code> otherwise.
     */
    private boolean isTokenCached(String series, String value)
    {
        if (tokenCache.containsKey(series) && isTokenInfoValid(tokenCache.get(series))
                && value.equals(tokenCache.get(series).getValue()))
        {
            return true;
        }
        return false;
    }

    /**
     * Needed for possibility to test.
     */
    public void setCachedTokenValidityTime(int cachedTokenValidityTime)
    {
        this.cachedTokenValidityTime = cachedTokenValidityTime;
    }
}
