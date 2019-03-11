package com.developmentontheedge.be5.server.services.rememberme;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

import javax.servlet.http.Cookie;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractRememberMeService implements RememberMeServices
{
    private static final Logger log = Logger.getLogger(AbstractRememberMeService.class.getName());

    public static final String REMEMBER_ME_KEY = "remember-me";
    private static final int TWO_WEEKS_S = 1209600;
    private static final int DEFAULT_SERIES_LENGTH = 16;
    private static final int DEFAULT_TOKEN_LENGTH = 16;
    private static final String DELIMITER = ":";

    private String cookieName = REMEMBER_ME_KEY;
    private int tokenValiditySeconds = TWO_WEEKS_S;
    private int seriesLength = DEFAULT_SERIES_LENGTH;
    private int tokenLength = DEFAULT_TOKEN_LENGTH;

    private final SecureRandom random;

    public AbstractRememberMeService()
    {
        this.random = new SecureRandom();
    }

    @Override
    public String autoLogin(Request request, Response response)
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

    protected abstract String processAutoLoginCookie(String[] cookieTokens, Request request,
                                                     Response response);

    @Override
    public void logout(Request request, Response response, String userName)
    {
        cancelCookie(request, response);
    }

    private String extractRememberMeCookie(Request request)
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

    String generateSeriesData()
    {
        byte[] newSeries = new byte[seriesLength];
        random.nextBytes(newSeries);
        return new String(Base64.getEncoder().encode(newSeries));
    }

    String generateTokenData()
    {
        byte[] newToken = new byte[tokenLength];
        random.nextBytes(newToken);
        return new String(Base64.getEncoder().encode(newToken));
    }

    void addCookie(PersistentRememberMeToken token, Request request,
                   Response response)
    {
        setCookie(new String[]{token.getSeries(), token.getTokenValue()},
                getTokenValiditySeconds(), request, response);
    }

    int getTokenValiditySeconds()
    {
        return tokenValiditySeconds;
    }

    public String getCookieName()
    {
        return cookieName;
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

    public void setCookieName(String cookieName)
    {
        this.cookieName = cookieName;
    }

    void setCookie(String[] tokens, int maxAge, Request request,
                           Response response)
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

    private void cancelCookie(Request request, Response response)
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
    String[] decodeCookie(String cookieValue) throws InvalidCookieException
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
    String encodeCookie(String[] cookieTokens)
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
