package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.server.authentication.rememberme.PersistentRememberMeToken;
import com.developmentontheedge.be5.server.authentication.rememberme.PersistentTokenRepository;

import java.sql.Timestamp;

public class RememberUserHelperMock implements PersistentTokenRepository
{
    @Override
    public void createNewToken(PersistentRememberMeToken token)
    {

    }

    @Override
    public void updateToken(String series, String tokenValue, Timestamp lastUsed)
    {

    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId)
    {
        return null;
    }

    @Override
    public void removeUserTokens(String username)
    {

    }
}
