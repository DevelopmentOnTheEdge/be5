package com.developmentontheedge.be5.server.services.rememberme;

import com.developmentontheedge.be5.meta.UserAwareMeta;
import com.developmentontheedge.be5.util.DateUtils;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * copy code parts from
 * org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices
 */
public class PersistentRememberMeServices extends AbstractRememberMeService implements RememberMeServices
{
    private static final Logger log = Logger.getLogger(PersistentRememberMeServices.class.getName());

    private final PersistentTokenRepository tokenRepository;
    private final UserAwareMeta userAwareMeta;

    @Inject
    public PersistentRememberMeServices(PersistentTokenRepository tokenRepository, UserAwareMeta userAwareMeta)
    {
        super();
        this.tokenRepository = tokenRepository;
        this.userAwareMeta = userAwareMeta;
    }

    protected String processAutoLoginCookie(String[] cookieTokens, Request request,
                                            Response response)
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

        if (token.getTimestamp().getTime() + getTokenValiditySeconds() * 1000L < System
                .currentTimeMillis())
        {
            throw new RememberMeAuthenticationException("Remember-me login has expired");
        }

        // Token also matches, so login is valid. Update the token value, keeping the
        // *same* series number.
        log.fine("Refreshing persistent login token for user '"
                + token.getUsername() + "', series '" + token.getSeries() + "'");

        PersistentRememberMeToken newToken = new PersistentRememberMeToken(
                token.getUsername(), token.getSeries(), generateTokenData(), DateUtils.currentTimestamp());

        try
        {
            tokenRepository.updateToken(newToken.getSeries(), newToken.getTokenValue(),
                    newToken.getTimestamp());
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
    public void logout(Request request, Response response, String username)
    {
        super.logout(request, response, username);
        if (username != null)
        {
            tokenRepository.removeUserTokens(username);
        }
    }

    public void onLoginSuccess(Request request,
                               Response response, String username)
    {
        log.fine("Creating new persistent login for user " + username);

        PersistentRememberMeToken persistentToken = new PersistentRememberMeToken(
                username, generateSeriesData(), generateTokenData(), DateUtils.currentTimestamp());
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

}
