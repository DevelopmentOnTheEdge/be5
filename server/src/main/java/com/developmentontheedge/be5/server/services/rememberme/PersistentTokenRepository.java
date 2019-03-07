package com.developmentontheedge.be5.server.services.rememberme;

import java.util.Date;

public interface PersistentTokenRepository
{
    void createNewToken(PersistentRememberMeToken token);

    void updateToken(String series, String tokenValue, Date lastUsed);

    PersistentRememberMeToken getTokenForSeries(String seriesId);

    void removeUserTokens(String username);
}
