package com.developmentontheedge.be5.server.services.rememberme;

import java.sql.Timestamp;

public interface PersistentTokenRepository
{
    void createNewToken(PersistentRememberMeToken token);

    void updateToken(String series, String tokenValue, Timestamp lastUsed);

    PersistentRememberMeToken getTokenForSeries(String seriesId);

    void removeUserTokens(String username);
}
