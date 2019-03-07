package com.developmentontheedge.be5.server.services.rememberme;

import com.developmentontheedge.be5.meta.UserAwareMeta;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * copy code parts from
 * org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices
 */
public class RememberMeServicesImpl implements RememberMeServices
{
    private static final Logger log = Logger.getLogger(RememberMeServicesImpl.class.getName());

    private static final String REMEMBER_ME_KEY = "remember-me";
    private static final int TWO_WEEKS_S = 1209600;
    private static final int DEFAULT_SERIES_LENGTH = 16;
    private static final int DEFAULT_TOKEN_LENGTH = 16;
    private static final String DELIMITER = ":";

    private String cookieName = REMEMBER_ME_KEY;
    private int tokenValiditySeconds = TWO_WEEKS_S;
    private int seriesLength = DEFAULT_SERIES_LENGTH;
    private int tokenLength = DEFAULT_TOKEN_LENGTH;

    private final PersistentTokenRepository tokenRepository;
    private final UserAwareMeta userAwareMeta;
    private final SecureRandom random;

    @Inject
    public RememberMeServicesImpl(PersistentTokenRepository tokenRepository, UserAwareMeta userAwareMeta)
    {
        this.tokenRepository = tokenRepository;
        this.userAwareMeta = userAwareMeta;
        random = new SecureRandom();
    }

    @Override
    public String autoLogin(HttpServletRequest request, HttpServletResponse response)
    {
        String rememberMeCookie = extractRememberMeCookie(request);

        if (rememberMeCookie == null)
        {
            return null;
        }

        log.fine("Remember-me cookie detected");

        if (rememberMeCookie.length() == 0)
        {
            log.fine("Cookie was empty");
            cancelCookie(request, response);
            return null;
        }

        try
        {
            String[] cookieTokens = decodeCookie(rememberMeCookie);

            return processAutoLoginCookie(cookieTokens, request, response);
        }
        catch (CookieTheftException cte)
        {
            cancelCookie(request, response);
            throw cte;
        }
        catch (InvalidCookieException invalidCookie)
        {
            log.fine("Invalid remember-me cookie: " + invalidCookie.getMessage());
        }
        catch (RememberMeAuthenticationException e)
        {
            log.fine(e.getMessage());
        }

        cancelCookie(request, response);
        return null;
    }

    private String extractRememberMeCookie(HttpServletRequest request)
    {
        Cookie[] cookies = request.getCookies();

        if ((cookies == null) || (cookies.length == 0))
        {
            return null;
        }

        for (Cookie cookie : cookies)
        {
            if (cookieName.equals(cookie.getName()))
            {
                return cookie.getValue();
            }
        }

        return null;
    }

    private String processAutoLoginCookie(String[] cookieTokens, HttpServletRequest request, HttpServletResponse response)
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
            throw new RememberMeAuthenticationException(
                    "No persistent token found for series id: " + presentedSeries);
        }

        // We have a match for this user/series combination
        if (!presentedToken.equals(token.getTokenValue()))
        {
            // Token doesn't match series value. Delete all logins for this user and throw
            // an exception to warn them.
            tokenRepository.removeUserTokens(token.getUsername());

            throw new CookieTheftException(
                    userAwareMeta.getLocalizedExceptionMessage("Invalid remember-me token (Series/token) mismatch. " +
                            "Implies previous cookie theft attack."));
        }

        if (token.getDate().getTime() + getTokenValiditySeconds() * 1000L < System
                .currentTimeMillis())
        {
            throw new RememberMeAuthenticationException("Remember-me login has expired");
        }

        // Token also matches, so login is valid. Update the token value, keeping the
        // *same* series number.
        log.fine("Refreshing persistent login token for user '"
                + token.getUsername() + "', series '" + token.getSeries() + "'");

        PersistentRememberMeToken newToken = new PersistentRememberMeToken(
                token.getUsername(), token.getSeries(), generateTokenData(), new Date());

        try
        {
            tokenRepository.updateToken(newToken.getSeries(), newToken.getTokenValue(),
                    newToken.getDate());
            addCookie(newToken, request, response);
        }
        catch (Exception e)
        {
            log.log(Level.SEVERE, "Failed to update token: ", e);
            throw new RememberMeAuthenticationException(
                    "Autologin failed due to data access problem");
        }

        return token.getUsername();
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, String username)
    {
        cancelCookie(request, response);
        if (username != null)
        {
            tokenRepository.removeUserTokens(username);
        }
    }

    public void onLoginSuccess(HttpServletRequest request,
                               HttpServletResponse response, String username)
    {
        log.fine("Creating new persistent login for user " + username);

        PersistentRememberMeToken persistentToken = new PersistentRememberMeToken(
                username, generateSeriesData(), generateTokenData(), new Timestamp(System.currentTimeMillis()));
        try
        {
            tokenRepository.createNewToken(persistentToken);
            addCookie(persistentToken, request, response);
        }
        catch (Exception e)
        {
            log.log(Level.SEVERE, "Failed to save persistent token ", e);
        }
    }

    private String generateSeriesData()
    {
        byte[] newSeries = new byte[seriesLength];
        random.nextBytes(newSeries);
        return new String(Base64.getEncoder().encode(newSeries));
    }

    private String generateTokenData()
    {
        byte[] newToken = new byte[tokenLength];
        random.nextBytes(newToken);
        return new String(Base64.getEncoder().encode(newToken));
    }

    private void addCookie(PersistentRememberMeToken token, HttpServletRequest request,
                           HttpServletResponse response)
    {
        setCookie(new String[]{token.getSeries(), token.getTokenValue()},
                getTokenValiditySeconds(), request, response);
    }

    private int getTokenValiditySeconds()
    {
        return tokenValiditySeconds;
    }

    public void setTokenValiditySeconds(int tokenValiditySeconds)
    {
        this.tokenValiditySeconds = tokenValiditySeconds;
    }

    public void setSeriesLength(int seriesLength)
    {
        this.seriesLength = seriesLength;
    }

    public void setTokenLength(int tokenLength)
    {
        this.tokenLength = tokenLength;
    }

    private void setCookie(String[] tokens, int maxAge, HttpServletRequest request,
                           HttpServletResponse response)
    {
        String cookieValue = encodeCookie(tokens);
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        if (maxAge < 1)
        {
            cookie.setVersion(1);
        }

        cookie.setHttpOnly(true);

        response.addCookie(cookie);
    }

    private void cancelCookie(HttpServletRequest request, HttpServletResponse response)
    {
        log.fine("Cancelling cookie");
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * Decodes the cookie and splits it into a set of token strings using the ":"
     * delimiter.
     *
     * @param cookieValue the value obtained from the submitted cookie
     * @return the array of tokens.
     * @throws InvalidCookieException if the cookie was not base64 encoded.
     */
    private String[] decodeCookie(String cookieValue) throws InvalidCookieException
    {
        for (int j = 0; j < cookieValue.length() % 4; j++)
        {
            cookieValue = cookieValue + "=";
        }

        try
        {
            Base64.getDecoder().decode(cookieValue.getBytes());
        }
        catch (IllegalArgumentException e)
        {
            throw new InvalidCookieException(
                    "Cookie token was not Base64 encoded; value was '" + cookieValue
                            + "'");
        }

        String cookieAsPlainText = new String(Base64.getDecoder().decode(cookieValue.getBytes()));

        String[] tokens = cookieAsPlainText.split(DELIMITER);

        for (int i = 0; i < tokens.length; i++)
        {
            try
            {
                tokens[i] = URLDecoder.decode(tokens[i], StandardCharsets.UTF_8.toString());
            }
            catch (UnsupportedEncodingException e)
            {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        return tokens;
    }

    /**
     * Inverse operation of decodeCookie.
     *
     * @param cookieTokens the tokens to be encoded.
     * @return base64 encoding of the tokens concatenated with the ":" delimiter.
     */
    private String encodeCookie(String[] cookieTokens)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cookieTokens.length; i++)
        {
            try
            {
                sb.append(URLEncoder.encode(cookieTokens[i], StandardCharsets.UTF_8.toString()));
            }
            catch (UnsupportedEncodingException e)
            {
                log.log(Level.SEVERE, e.getMessage(), e);
            }

            if (i < cookieTokens.length - 1)
            {
                sb.append(DELIMITER);
            }
        }

        String value = sb.toString();

        sb = new StringBuilder(new String(Base64.getEncoder().encode(value.getBytes())));

        while (sb.charAt(sb.length() - 1) == '=')
        {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

}
