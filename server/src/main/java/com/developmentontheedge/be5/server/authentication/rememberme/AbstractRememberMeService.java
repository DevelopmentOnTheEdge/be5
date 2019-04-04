package com.developmentontheedge.be5.server.authentication.rememberme;

import com.developmentontheedge.be5.util.Utils;
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

/**
 * copy code parts from
 * org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices
 */
public abstract class AbstractRememberMeService implements RememberMeServices
{
    private static final Logger log = Logger.getLogger(AbstractRememberMeService.class.getName());

    public static final String REMEMBER_ME_KEY = "remember-me";
    public static final String DEFAULT_PARAMETER = "remember-me";
    private static final int TWO_WEEKS_S = 1209600;
    private static final int DEFAULT_SERIES_LENGTH = 16;
    private static final int DEFAULT_TOKEN_LENGTH = 16;
    private static final String DELIMITER = ":";

    private String cookieName = REMEMBER_ME_KEY;
    private String parameter = DEFAULT_PARAMETER;
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

    @Override
    public final void loginSuccess(Request request, Response response, String username)
    {
        if (!rememberMeRequested(request, parameter))
        {
            log.fine("Remember-me login not requested.");
            return;
        }

        onLoginSuccess(request, response, username);
    }

    private boolean rememberMeRequested(Request request, String parameter)
    {
        String paramValue = request.get(parameter);

        if (paramValue != null)
        {
            if (paramValue.equalsIgnoreCase("true") || paramValue.equalsIgnoreCase("on")
                    || paramValue.equalsIgnoreCase("yes") || paramValue.equals("1"))
            {
                return true;
            }
        }

        if (log.isLoggable(Level.FINE))
        {
            log.fine("Did not send remember-me cookie (principal did not set parameter '"
                    + parameter + "')");
        }

        return false;
    }

    /**
     * Called from loginSuccess when a remember-me login has been requested. Typically
     * implemented by subclasses to set a remember-me cookie and potentially store a
     * record of it if the implementation requires this.
     */
    protected abstract void onLoginSuccess(Request request, Response response, String username);

    protected abstract String processAutoLoginCookie(String[] cookieTokens, Request request,
                                                     Response response);

    @Override
    public void logout(Request request, Response response, String userName)
    {
        cancelCookie(request, response);
    }

    String extractRememberMeCookie(Request request)
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

    public void setParameter(String parameter)
    {
        if (Utils.isEmpty(parameter)) throw new IllegalArgumentException("Parameter name cannot be empty or null");
        this.parameter = parameter;
    }

    public String getParameter()
    {
        return parameter;
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

    void cancelCookie(Request request, Response response)
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
